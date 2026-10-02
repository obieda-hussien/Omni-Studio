package com.novacut.editor.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

/** Children own clip/ruler edits; vertical swipes remain available to the track stack. */
internal suspend fun PointerInputScope.detectTimelineViewportGestures(
    initialViewport: () -> TimelineViewport,
    totalDurationMs: () -> Long,
    onGestureStarted: () -> Unit,
    onGestureEnded: () -> Unit,
    onViewportChanged: (TimelineViewport) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var viewport = initialViewport()
        var accumulatedPan = Offset.Zero
        var accumulatedZoom = 1f
        var active = false
        try {
            do {
                val event = awaitPointerEvent()
                if (event.changes.any { it.isConsumed }) break
                val pan = event.calculatePan()
                val zoom = event.calculateZoom()
                if (!pan.x.isFinite() || !pan.y.isFinite() || !zoom.isFinite()) break
                accumulatedPan += pan
                accumulatedZoom *= zoom
                val multiTouch = event.changes.count { it.pressed } > 1
                if (!active) {
                    // Do not swallow a vertical track-list scroll as a timeline pan.
                    if (!multiTouch && abs(accumulatedPan.y) > viewConfiguration.touchSlop &&
                        abs(accumulatedPan.y) > abs(accumulatedPan.x)) break
                    val zoomMotion = abs(1f - accumulatedZoom) * event.calculateCentroidSize(useCurrent = false)
                    active = abs(accumulatedPan.x) > viewConfiguration.touchSlop ||
                        (multiTouch && zoomMotion > viewConfiguration.touchSlop)
                    if (active) onGestureStarted()
                }
                if (active) {
                    viewport = viewport.transform(
                        centroidXPx = event.calculateCentroid(useCurrent = false).x,
                        panXPx = pan.x,
                        zoomFactor = zoom,
                        widthPx = size.width.toFloat(),
                        totalDurationMs = totalDurationMs(),
                    )
                    onViewportChanged(viewport)
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } while (event.changes.any { it.pressed })
        } finally {
            // Includes pointer-input cancellation on rotation, navigation or disposal.
            if (active) onGestureEnded()
        }
    }
}
