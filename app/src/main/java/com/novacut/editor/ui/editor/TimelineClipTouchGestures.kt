package com.novacut.editor.ui.editor

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope

/** Edge trims start at touch slop. The body belongs to viewport pan until a long press. */
internal suspend fun PointerInputScope.detectTimelineClipTouchGestures(
    zoneAt: (Offset) -> TimelineClipGestureZone,
    onStart: (Offset, TimelineClipGestureZone) -> Unit,
    onDelta: (PointerInputChange, Float) -> Unit,
    onEnd: () -> Unit,
    onCancel: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val zone = zoneAt(down.position)
        if (zone == TimelineClipGestureZone.NONE) return@awaitEachGesture
        var active = false
        var finished = false
        try {
            val start = if (zone == TimelineClipGestureZone.TRIM_LEFT || zone == TimelineClipGestureZone.TRIM_RIGHT) {
                var initialDelta = 0f
                val slop = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                    change.consume()
                    initialDelta = overSlop
                }
                if (slop != null) {
                    onStart(down.position, zone)
                    active = true
                    onDelta(slop, initialDelta)
                }
                slop
            } else {
                val held = awaitLongPressOrCancellation(down.id)
                if (held != null) {
                    held.consume()
                    onStart(down.position, zone)
                    active = true
                }
                held
            }
            if (start != null) {
                finished = horizontalDrag(start.id) { change ->
                    onDelta(change, change.position.x - change.previousPosition.x)
                    change.consume()
                }
            }
        } finally {
            // Includes coroutine cancellation when the clip disappears or the device rotates.
            if (active) {
                if (finished) onEnd() else onCancel()
            }
        }
    }
}
