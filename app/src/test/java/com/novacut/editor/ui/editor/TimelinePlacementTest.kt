package com.novacut.editor.ui.editor

import android.net.FakeUri
import com.novacut.editor.model.*
import org.junit.Assert.*
import org.junit.Test

class TimelinePlacementTest {
    private val timebase = TimelineTimebase(30)
    private fun clip(id: String = "image", start: Long = 2000, duration: Long = 3000, linked: String? = null, image: Boolean = false) =
        Clip(id = id, sourceUri = FakeUri, sourceDurationMs = duration, timelineStartMs = start,
            linkedClipId = linked, isStillImage = image)
    private fun track(vararg clips: Clip, locked: Boolean = false) = Track(id = "visual", type = TrackType.VIDEO, index = 0, clips = clips.toList(), isLocked = locked)

    @Test fun moveFromTwoToFiveSecondsPreservesDurationAndNeighbours() {
        val a = clip("a", 0, 1000)
        val b = clip()
        val c = clip("c", 10000, 2000)
        val result = moveLinkedClips(listOf(track(a, b, c)), b.id, 3000, timebase).single().clips
        assertEquals(a, result[0])
        assertEquals(5000L, result[1].timelineStartMs)
        assertEquals(3000L, result[1].durationMs)
        assertEquals(c, result[2])
    }

    @Test fun collisionStopsAtLegalBoundaryAndDoesNotTrimTheOtherClip() {
        val b = clip()
        val c = clip("c", 6000, 3000)
        val result = moveLinkedClips(listOf(track(b, c)), b.id, 2000, timebase).single().clips
        assertEquals(3000L, result[0].timelineStartMs)
        assertEquals(c, result[1])
    }

    @Test fun movingAcrossAnotherClipCanLandInTheNextFreeGap() {
        val b = clip()
        val c = clip("c", 6000, 3000)
        val result = moveLinkedClips(listOf(track(b, c)), b.id, 8000, timebase).single().clips
        assertEquals(c, result[0])
        assertEquals(10000L, result[1].timelineStartMs)
    }

    @Test fun linkedAudioMovesTogetherAndAnyLockedMemberBlocksTheWholeMove() {
        val v = clip("v", linked = "a")
        val a = clip("a", linked = "v")
        val tracks = listOf(track(v), track(a).copy(id = "audio", type = TrackType.AUDIO, index = 1))
        val moved = moveLinkedClips(tracks, "v", 3000, timebase)
        assertEquals(listOf(5000L, 5000L), moved.map { it.clips.single().timelineStartMs })
        val locked = listOf(tracks[0], tracks[1].copy(isLocked = true))
        assertEquals(locked, moveLinkedClips(locked, "v", 3000, timebase))
    }

    @Test fun movementCannotPassBeforeZero() {
        val result = moveLinkedClips(listOf(track(clip())), "image", -6000, timebase)
        assertEquals(0L, result.single().clips.single().timelineStartMs)
    }

    @Test fun imageCanBecomeEightSecondsWhileAudioCannotExceedItsSource() {
        val image = planClipTiming(listOf(track(clip(image = true))), "image", 5000, 8000, timebase).single().clips.single()
        assertEquals(5000L, image.timelineStartMs)
        assertEquals(8000L, image.durationMs)
        assertEquals(8000L, image.sourceDurationMs)
        val audio = planClipTiming(listOf(track(clip()).copy(type = TrackType.AUDIO)), "image", 5000, 8000, timebase).single().clips.single()
        assertEquals(3000L, audio.durationMs)
    }

    @Test fun imageHandleCanExtendBeyondOriginalLengthAndSpeedIsAccountedFor() {
        val image = clip(start = 0, duration = 3000, image = true)
        val action = resolveTimelineClipGestureAction(TimelineClipGestureZone.TRIM_RIGHT, image, 750f, 0.15f)
        assertEquals(TimelineClipGestureAction.TrimRight(8000), action)
        val fast = image.copy(speed = 2f)
        assertEquals(TimelineClipGestureAction.TrimRight(5000),
            resolveTimelineClipGestureAction(TimelineClipGestureZone.TRIM_RIGHT, fast, 150f, 0.15f))
    }

    @Test fun fractionalFrameRatesKeepLinkedOffsetsAndDoNotOverlapNeighbours() {
        val ntsc = TimelineTimebase.NTSC_29_97
        val v = clip("v", ntsc.timeMsAt(60), 3003, linked = "a")
        val a = v.copy(id = "a", linkedClipId = "v")
        val obstacle = clip("next", 6100, 2000)
        val tracks = listOf(track(v, obstacle), track(a).copy(id = "audio", type = TrackType.AUDIO, index = 1))
        val moved = moveLinkedClips(tracks, "v", 2000, ntsc)
        val visual = moved[0].clips.first { it.id == "v" }
        val audio = moved[1].clips.single()
        assertEquals(visual.timelineStartMs, audio.timelineStartMs)
        assertTrue(visual.timelineEndMs <= obstacle.timelineStartMs)
        assertEquals(obstacle, moved[0].clips.first { it.id == "next" })
    }

    @Test fun invalidTimingDoesNotMutateTheProject() {
        val tracks = listOf(track(clip(image = true)))
        assertEquals(tracks, planClipTiming(tracks, "image", -1, 8000, timebase))
        assertEquals(tracks, planClipTiming(tracks, "image", 5000, Long.MAX_VALUE, timebase))
    }

    @Test fun edgeAutoScrollIsGradualAndCentreStaysStillOnSmallScreens() {
        assertEquals(-1f, timelineEdgeScrollVelocity(0f, 300f, 48f), 0f)
        assertEquals(-0.5f, timelineEdgeScrollVelocity(24f, 300f, 48f), 0f)
        assertEquals(0f, timelineEdgeScrollVelocity(150f, 300f, 48f), 0f)
        assertEquals(1f, timelineEdgeScrollVelocity(300f, 300f, 48f), 0f)
        assertEquals(0f, timelineEdgeScrollVelocity(20f, 40f, 48f), 0f)
        assertEquals(0f, timelineEdgeScrollVelocity(Float.NaN, 300f, 48f), 0f)
    }
}
