package com.novacut.editor.engine

import java.util.Locale

/** Smoothing retains the original duration, dimensions and audio timebase. */
internal fun motionInterpolationFilter(targetFps: Int, sourceDurationMs: Long): String {
    require(targetFps in 24..120)
    require(sourceDurationMs > 0)
    val duration = String.format(Locale.US, "%.3f", sourceDurationMs / 1000.0)
    // MCI buffers neighbouring frames; padding before it prevents the final frames being lost.
    return "setpts=PTS-STARTPTS,tpad=stop_mode=clone:stop_duration=1," +
        "minterpolate=fps=$targetFps:mi_mode=mci:mc_mode=aobmc:me_mode=bidir:vsbmc=1:scd=fdiff," +
        "trim=duration=$duration,setpts=PTS-STARTPTS"
}
