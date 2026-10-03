package com.novacut.editor.ui.editor

import org.junit.Assert.*
import org.junit.Test

class TimelineViewportTest {
    @Test fun smallPointerMovesAccumulateWithoutWaitingForRecomposition() {
        var viewport = TimelineViewport(10f, 1000.0)
        repeat(120) { viewport = viewport.transform(100f, -0.25f, 1f, 300f, 60_000L) }
        assertEquals(1020.0, viewport.scrollMs, 0.001)
        repeat(120) { viewport = viewport.transform(100f, 0.25f, 1f, 300f, 60_000L) }
        assertEquals(1000.0, viewport.scrollMs, 0.001)
    }

    @Test fun zoomKeepsTheTimeUnderThePinchCenter() {
        val before = TimelineViewport(1f, 5000.0)
        val after = before.transform(150f, 0f, 2f, 600f, 60_000L)
        val oldAnchor = before.scrollMs + 150.0 / (before.zoom * TIMELINE_PIXELS_PER_MS)
        val newAnchor = after.scrollMs + 150.0 / (after.zoom * TIMELINE_PIXELS_PER_MS)
        assertEquals(oldAnchor, newAnchor, 0.001)
        assertEquals(2f, after.zoom, 0f)
    }

    @Test fun viewportStopsAtProjectBoundsAndReversesImmediately() {
        val end = TimelineViewport(1f, 0.0).transform(0f, -100_000f, 1f, 300f, 10_000L)
        assertEquals(maxTimelineViewportScrollMs(10_000L, 2000L).toDouble(), end.scrollMs, 0.001)
        val reverse = end.transform(0f, 15f, 1f, 300f, 10_000L)
        assertEquals(end.scrollMs - 100.0, reverse.scrollMs, 0.001)
        assertEquals(0.0, reverse.transform(0f, 100_000f, 1f, 300f, 10_000L).scrollMs, 0.0)
    }

    @Test fun invalidInputAndEmptyProjectsStayFinite() {
        val result = TimelineViewport(Float.NaN, Double.NaN)
            .transform(Float.NaN, Float.POSITIVE_INFINITY, Float.NaN, 0f, 0L)
        assertTrue(result.zoom.isFinite())
        assertEquals(0.0, result.scrollMs, 0.0)
        assertEquals(0L, maxTimelineViewportScrollMs(0L, 100L))
        assertEquals(10_000L, maxTimelineViewportScrollMs(10_000L, null))
    }

    @Test fun overviewGrabDoesNotJumpToTheCenterOfTheViewport() {
        assertEquals(2000L, timelineOverviewDragStartOffset(22f, 100f, 10_000L, 2000L, 2000L))
        assertEquals(6000L, timelineOverviewDragStartOffset(70f, 100f, 10_000L, 2000L, 2000L))
        assertEquals(8750L, timelineOverviewScrollOffsetForTap(100f, 100f, 10_000L, 2000L, 0L))
    }

    @Test fun shortWidePhonesUseTwoColumnsAndPortraitKeepsTheStack() {
        assertTrue(useSideBySideEditor(800, 360))
        assertTrue(useSideBySideEditor(640, 320))
        assertFalse(useSideBySideEditor(360, 800))
        assertFalse(useSideBySideEditor(600, 360))
        assertFalse(useSideBySideEditor(1000, 800))
    }
}
