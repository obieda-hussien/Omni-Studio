#!/usr/bin/env python3
"""Offline inference/encode smoke test. Pass already downloaded, revision-pinned ONNX models.

Requires numpy, Pillow, onnxruntime and desktop ffmpeg/ffprobe. This verifies model
contracts and image-sequence encoding; Android decoder/provider/device tests remain separate.
"""
import argparse
import hashlib
import json
import subprocess
import tempfile
from pathlib import Path

import numpy as np
import onnxruntime as ort
from PIL import Image

MODELS = {
    "rife49_ensemble_True_scale_1_sim.onnx": "76e4cef9ab42fa7dd4e8f6e4aba47462051e3faa969e4bca6479784fbab0ac6f",
    "realesr-general-x4v3.onnx": "924ebad6532777303582d4ce7811849b88869231a3ae7093e0f21200249df8d5",
}


def run(*args):
    return subprocess.check_output(args, stderr=subprocess.PIPE, text=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--model-dir", type=Path, required=True)
    args = parser.parse_args()
    options = ort.SessionOptions()
    options.intra_op_num_threads = 4
    sessions = []
    for filename, expected in MODELS.items():
        path = args.model_dir / filename
        assert hashlib.sha256(path.read_bytes()).hexdigest() == expected, filename
        sessions.append(ort.InferenceSession(str(path), options, providers=["CPUExecutionProvider"]))
    rife, upscale = sessions
    assert {x.name for x in rife.get_inputs()} == {"img0", "img1", "timestep"}
    assert {x.name for x in upscale.get_inputs()} == {"input"}
    first = np.zeros((1, 3, 64, 96), np.float32)
    second = first.copy()
    first[:, :, 24:40, 16:32] = 1
    second[:, :, 24:40, 32:48] = 1
    midpoint = rife.run(None, {"img0": first, "img1": second, "timestep": np.array([0.5], np.float32)})[0]
    assert midpoint.shape == first.shape and np.isfinite(midpoint).all()
    assert np.abs(midpoint - (first + second) / 2).max() > 0.01, "Neural motion must differ from a crossfade"
    identity = rife.run(None, {"img0": first, "img1": first, "timestep": np.array([0.5], np.float32)})[0]
    # The model is approximate near edges. The Android path bypasses inference for identical inputs.
    assert np.abs(identity - first).mean() < 0.001 and np.abs(identity - first).max() < 0.04

    # Verify overlapping tile context against whole-image inference, including partial edge tiles.
    image = np.random.default_rng(42).uniform(0, 1, (1, 3, 65, 257)).astype(np.float32)
    whole = upscale.run(None, {"input": image})[0]
    tiled = np.empty_like(whole)
    for x in range(0, 257, 128):
        left, right = max(0, x - 40), min(257, x + 128 + 40)
        core = min(128, 257 - x)
        tile = upscale.run(None, {"input": image[:, :, :, left:right].copy()})[0]
        offset = (x - left) * 4
        tiled[:, :, :, x * 4:(x + core) * 4] = tile[:, :, :, offset:offset + core * 4]
    assert whole.shape == (1, 3, 260, 1028) and np.isfinite(whole).all()
    assert np.max(np.abs(whole - tiled)) < 1e-4, "Tile boundaries must retain model context"

    with tempfile.TemporaryDirectory(prefix="omni-neural-test-") as directory:
        root = Path(directory)
        # One second of audio in a two-second video catches accidental -shortest truncation.
        run("ffmpeg", "-y", "-f", "lavfi", "-i", "color=size=96x64:rate=12:duration=2",
            "-f", "lavfi", "-i", "sine=frequency=440:duration=1", "-c:v", "mpeg4", "-c:a", "aac", str(root / "source.mp4"))
        for index in range(48):
            value = first if index % 2 == 0 else midpoint
            rgb = np.clip(value[0].transpose(1, 2, 0) * 255, 0, 255).round().astype(np.uint8)
            Image.fromarray(rgb).save(root / f"frame_{index:05d}.png")
        run("ffmpeg", "-y", "-framerate", "24.0", "-i", str(root / "frame_%05d.png"),
            "-i", str(root / "source.mp4"), "-map", "0:v:0", "-map", "1:a:0?", "-c:v", "mpeg4",
            "-pix_fmt", "yuv420p", "-c:a", "aac", "-t", "2.000000", str(root / "output.mp4"))
        streams = json.loads(run("ffprobe", "-v", "error", "-count_frames", "-show_streams", "-of", "json", str(root / "output.mp4")))["streams"]
        video = next(s for s in streams if s["codec_type"] == "video")
        assert video["width"] == 96 and video["height"] == 64 and int(video["nb_read_frames"]) == 48
        assert abs(float(video["duration"]) - 2) < 0.001
        assert any(s["codec_type"] == "audio" for s in streams)
    print("PASS: model hashes, RIFE motion/static frames, Real-ESRGAN tiles, video duration/dimensions/audio")


if __name__ == "__main__":
    main()
