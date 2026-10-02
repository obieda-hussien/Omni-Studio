package com.novacut.editor.ui.editor

internal fun timelineOverviewScrollOffsetForTap(
    xPx: Float,
    widthPx: Float,
    totalDurationMs: Long,
    visibleDurationMs: Long,
    currentScrollOffsetMs: Long
): Long {
    if (!xPx.isFinite() || !widthPx.isFinite() || widthPx <= 0f || totalDurationMs <= 0L) return currentScrollOffsetMs
    val fraction = (xPx / widthPx).coerceIn(0f, 1f)
    val targetMs = (fraction * totalDurationMs).toLong()
    return (targetMs - visibleDurationMs.coerceAtLeast(0L) / 2)
        .coerceIn(0L, maxTimelineViewportScrollMs(totalDurationMs, visibleDurationMs))
}


/** Grabbing inside the viewport keeps the grab point; outside it recenters before dragging. */
internal fun timelineOverviewDragStartOffset(
    xPx: Float,
    widthPx: Float,
    totalDurationMs: Long,
    visibleDurationMs: Long,
    currentScrollOffsetMs: Long,
): Long {
    if (!xPx.isFinite() || !widthPx.isFinite() || widthPx <= 0f || totalDurationMs <= 0L) {
        return currentScrollOffsetMs
    }
    val timeAtPointer = xPx.toDouble() / widthPx * totalDurationMs
    return if (timeAtPointer >= currentScrollOffsetMs &&
        timeAtPointer <= currentScrollOffsetMs.toDouble() + visibleDurationMs) {
        currentScrollOffsetMs
    } else timelineOverviewScrollOffsetForTap(
        xPx, widthPx, totalDurationMs, visibleDurationMs, currentScrollOffsetMs,
    )
}
