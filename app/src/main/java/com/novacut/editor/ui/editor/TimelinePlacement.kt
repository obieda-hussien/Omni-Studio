package com.novacut.editor.ui.editor

import com.novacut.editor.model.Track
import com.novacut.editor.model.TimelineTimebase
import kotlin.math.abs

internal const val MAX_STILL_IMAGE_DURATION_MS = 86_400_000L

/** Move linked media through legal gaps without changing any neighbour's source window. */
internal fun moveLinkedClips(
    tracks: List<Track>, clipId: String, requestedDeltaMs: Long, timebase: TimelineTimebase,
): List<Track> {
    val ids = linkedClipIds(tracks, clipId)
    val members = tracks.flatMap { track -> track.clips.filter { it.id in ids }.map { track to it } }
    if (members.isEmpty() || members.any { it.first.isLocked }) return tracks
    val requestedFrames = timebase.frameIndexAt(abs(requestedDeltaMs)) * if (requestedDeltaMs < 0) -1 else 1
    val candidates = mutableSetOf(requestedFrames, 0L)
    for ((track, clip) in members) {
        val originFrame = timebase.frameIndexAt(clip.timelineStartMs)
        candidates += -originFrame
        for (other in track.clips) {
            if (other.id in ids) continue
            val latestStart = other.timelineStartMs - clip.durationMs
            if (latestStart >= 0L) candidates += timebase.frameIndexAtOrBefore(latestStart) - originFrame
            candidates += timebase.frameIndexAtOrAfter(other.timelineEndMs) - originFrame
        }
    }
    val deltaFrames = candidates.filter { candidate ->
        members.all { (track, clip) ->
            val originFrame = timebase.frameIndexAt(clip.timelineStartMs)
            val start = timebase.addFrames(clip.timelineStartMs, candidate)
            val end = start + clip.durationMs
            originFrame + candidate >= 0L && track.clips.none { other ->
                other.id !in ids && start < other.timelineEndMs && end > other.timelineStartMs
            }
        }
    }.minByOrNull { abs(it - requestedFrames) } ?: return tracks
    if (deltaFrames == 0L) return tracks
    return tracks.map { track -> track.copy(clips = track.clips.map { clip ->
        if (clip.id in ids) clip.copy(timelineStartMs = timebase.addFrames(clip.timelineStartMs, deltaFrames)) else clip
    }.sortedBy { it.timelineStartMs }) }
}

/** Signed, gradual edge velocity; the centre remains still even on a narrow phone. */
internal fun timelineEdgeScrollVelocity(pointerXPx: Float, widthPx: Float, edgePx: Float): Float {
    if (!pointerXPx.isFinite() || !widthPx.isFinite() || !edgePx.isFinite() || widthPx <= 0f || edgePx <= 0f) return 0f
    val edge = edgePx.coerceAtMost(widthPx / 3f)
    return when {
        pointerXPx < edge -> -((edge - pointerXPx) / edge).coerceIn(0f, 1f)
        pointerXPx > widthPx - edge -> ((pointerXPx - widthPx + edge) / edge).coerceIn(0f, 1f)
        else -> 0f
    }
}

internal fun extendStillImageWindow(tracks: List<Track>, clipId: String, requestedEndMs: Long): List<Track> =
    tracks.map { track -> track.copy(clips = track.clips.map { clip ->
        if (clip.id == clipId && clip.isStillImage) clip.copy(
            sourceDurationMs = maxOf(clip.sourceDurationMs, requestedEndMs.coerceIn(0L, MAX_STILL_IMAGE_DURATION_MS)),
        ) else clip
    }) }

internal fun planClipTiming(
    tracks: List<Track>, clipId: String, startMs: Long, durationMs: Long, timebase: TimelineTimebase,
): List<Track> {
    if (startMs !in 0L..MAX_STILL_IMAGE_DURATION_MS || durationMs !in MIN_TIMELINE_CLIP_DURATION_MS..MAX_STILL_IMAGE_DURATION_MS) return tracks
    val clip = tracks.findClipLocation(clipId)?.clip ?: return tracks
    val ids = linkedClipIds(tracks, clipId)
    if (tracks.any { it.isLocked && it.clips.any { c -> c.id in ids } }) return tracks
    var candidate = moveLinkedClips(tracks, clipId, startMs - clip.timelineStartMs, timebase)
    if (clip.isStillImage) {
        candidate = extendStillImageWindow(candidate, clipId,
            clip.trimStartMs + (durationMs * clip.speed.toDouble()).toLong())
    }
    val moved = candidate.findClipLocation(clipId)?.clip ?: return tracks
    return trimLinkedClipEndToTimelineTime(candidate, clipId, ids,
        timebase.snapMs(moved.timelineStartMs + durationMs), timebase)
}
