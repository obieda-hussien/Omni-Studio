package com.novacut.editor.ui.editor

import kotlin.math.roundToLong

internal const val TIMELINE_PIXELS_PER_MS = 0.15f

/** Fractional time is retained across pointer events, independently of recomposition. */
internal data class TimelineViewport(val zoom: Float, val scrollMs: Double) {
    fun transform(
        centroidXPx: Float,
        panXPx: Float,
        zoomFactor: Float,
        widthPx: Float,
        totalDurationMs: Long,
    ): TimelineViewport {
        val oldZoom = TimelineToolbarPolicy.clampZoom(zoom)
        val factor = zoomFactor.takeIf { it.isFinite() && it > 0f } ?: 1f
        val newZoom = TimelineToolbarPolicy.clampZoom(oldZoom * factor)
        val centroid = centroidXPx.takeIf(Float::isFinite)?.toDouble() ?: 0.0
        val pan = panXPx.takeIf(Float::isFinite)?.toDouble() ?: 0.0
        val oldPpm = oldZoom * TIMELINE_PIXELS_PER_MS.toDouble()
        val newPpm = newZoom * TIMELINE_PIXELS_PER_MS.toDouble()
        val oldScroll = scrollMs.takeIf(Double::isFinite)?.coerceAtLeast(0.0) ?: 0.0
        val anchorMs = oldScroll + centroid / oldPpm
        val visibleMs = if (widthPx.isFinite() && widthPx > 0f) {
            (widthPx / newPpm).roundToLong().coerceAtLeast(1L)
        } else null
        val maxScroll = maxTimelineViewportScrollMs(totalDurationMs, visibleMs).toDouble()
        return TimelineViewport(newZoom, (anchorMs - (centroid + pan) / newPpm).coerceIn(0.0, maxScroll))
    }
}

/** Shared by gesture accumulation and the ViewModel, including a small project lead-out. */
internal fun maxTimelineViewportScrollMs(totalDurationMs: Long, visibleDurationMs: Long?): Long {
    val duration = totalDurationMs.coerceAtLeast(0L)
    if (duration == 0L) return 0L
    val visible = visibleDurationMs?.coerceAtLeast(1L) ?: return duration
    val padding = (visible / 4L).coerceIn(750L, 6_000L)
    val minimumVisible = (visible - padding).coerceAtLeast((visible / 2L).coerceAtLeast(1L))
    return (duration - minimumVisible).coerceAtLeast(0L)
}
