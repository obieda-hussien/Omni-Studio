# Local neural media processing

## Available tools

**RIFE smooth motion** uses Practical-RIFE 4.9 through the bundled ONNX Runtime. It doubles the source cadence without changing frame dimensions or clip timing. It is independent of the existing built-in FFmpeg **Smooth motion** tool. RIFE is an explicit 21,458,882-byte model download; no NCNN, Vulkan, CUDA or Python runtime is added to the Android app.

**AI Upscale** uses the compact Real-ESRGAN general x4v3 model. The model predicts 4x detail in overlapping tiles; each disjoint tile core is downsampled to the advertised 2x output. Tiles have 40 pixels of context. The app never allocates a full 4x result. Images retain their timeline duration; videos retain cadence and audio.

Both models have immutable source revisions and exact SHA-256 pins in [models.md](models.md). Inference verifies the complete file before creating its session. Downloads respect the existing Wi-Fi-only preference, network validation, transfer ceiling and atomic file activation. Download, inference and deletion share a per-model mutex. Model licenses are included in the in-app open-source notices.

## Resource limits and failure behavior

- RIFE: SDR motion video up to 921,600 input pixels, at most 1280 pixels on either side, source cadence up to 60 fps. Portrait 720x1280 is supported.
- Upscale: 2x video output up to 1920x1080 pixels by area; 2x still output up to 4096x4096 pixels. The longest output edge is bounded to 4096.
- All frame pipelines: at most 3,600 output frames and 1 GiB temporary frame storage. The preflight reserves worst-case PNG bytes plus space for the final encode and a 128 MiB free-space margin. A job exceeding these limits is rejected in full, rather than truncated or silently downscaled.
- The neural tools reject known HDR at the editor gate and also inspect encoded color transfer in the video engine. SDR output does not inherit obsolete source color metadata.
- Decode keeps only two source frames and one rendered frame resident. RIFE uses edge-repeat padding to multiples of 32 and crops the result. Identical inputs bypass inference. Still-image decoding checks dimensions before allocating and applies EXIF orientation.
- Output is CFR. Encoded timestamps recover fractional cadence from rounded metadata. Variable-rate inputs are resampled to the measured average cadence; this does not preserve per-frame VFR timestamps.
- FFmpeg image-sequence encoding maps optional source audio, supports fractional fps and sets the output duration. It does not use `-shortest`, so a shorter audio stream cannot cut the video. Container duration can differ by up to a frame or codec padding; existing clip timing stays unchanged.
- Cancellation is checked between decode/inference steps and tiles; an already executing synchronous ONNX call finishes before resources close. Temporary frames and failed output files are removed. Neural inference speed and memory use still depend on the Android device; this is offline batch processing, not a real-time preview effect.
- Before applying output, the delegate re-checks that the clip still exists, is unchanged and is unlocked. It creates one undo entry, clears obsolete proxies and saves the project. Cancellation/failure/stale edits retain the original source.

## Verification

Executed locally: actual ONNX model downloads and checksum verification, CPU inference against the real models, RIFE motion output distinguished from crossfading, approximate static-frame stability, Real-ESRGAN partial-tile equivalence to full inference, and a two-second video encode with only one second of source audio. The encoded video retains two seconds, 48 frames at 24 fps, dimensions and an audio stream.

The exact new engine files compile with Kotlin 2.4.10 against Android and ONNX Runtime 1.26.0 APIs, with application dependency stubs. Focused JVM tests cover resource limits, fractional cadence, registry gates and tile coverage. Full integration is checked separately by Android CI; CPU smoke tests do not validate Android hardware/provider behavior.

Reproduce model smoke tests after downloading the pinned artifacts:

```sh
python scripts/verify_neural_models.py --model-dir /path/to/pinned-models
./gradlew :app:testQaUnitTest :app:compileQaAndroidTestKotlin
```

Device review: download both models on Wi-Fi, retry an interrupted download, process a short clip with audio, process a rotated JPEG, cancel mid-job, edit or lock a clip while processing, verify undo/redo, and remove a model while another operation owns it. Repeat on a low-memory ARM device and confirm oversized sources explain their limit and keep the original media.
