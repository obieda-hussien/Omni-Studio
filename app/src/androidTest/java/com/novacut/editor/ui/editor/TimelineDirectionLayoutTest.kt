package com.novacut.editor.ui.editor

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.novacut.editor.engine.VideoEngine
import com.novacut.editor.model.Clip
import com.novacut.editor.model.Track
import com.novacut.editor.model.TrackType
import com.novacut.editor.ui.ClearCutTestTags
import com.novacut.editor.ui.theme.ClearCutTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimelineTestDependencies { fun videoEngine(): VideoEngine }

/** Exercises the real timeline under an RTL host while all app strings remain English. */
class TimelineDirectionLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun hostDirectionDoesNotMirrorClipsAndDraggingRightAdvancesTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = EntryPointAccessors.fromApplication(context, TimelineTestDependencies::class.java).videoEngine()
        var direction by mutableStateOf(LayoutDirection.Ltr)
        var slideDelta = 0L
        val clip = Clip(id = "direction-clip", sourceUri = Uri.parse("content://test/audio"),
            sourceDurationMs = 8000L, timelineStartMs = 1500L, trimEndMs = 4000L)
        var clips by mutableStateOf(listOf(clip))
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                ClearCutTheme {
                    Timeline(
                        tracks = listOf(Track(id = "audio", type = TrackType.AUDIO, index = 0, clips = clips)),
                        playheadMs = 0L, totalDurationMs = 12_000L,
                        zoomLevel = 0.4f, scrollOffsetMs = 0L, selectedClipId = clip.id,
                        onClipSelected = { _, _ -> }, onPlayheadMoved = {},
                        onZoomChanged = {}, onScrollChanged = {},
                        onSlideClip = { _, delta ->
                            slideDelta = delta
                            clips = listOf(clip.copy(timelineStartMs = clip.timelineStartMs + delta))
                        },
                        compactLayout = true, engine = engine,
                        modifier = Modifier.width(360.dp).height(320.dp),
                    )
                }
            }
        }
        val tag = ClearCutTestTags.TIMELINE_CLIP_PREFIX + clip.id
        val ltrBounds = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { direction = LayoutDirection.Rtl }
        val rtlBounds = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        assertEquals(ltrBounds.left, rtlBounds.left, 0.5f)
        assertEquals(ltrBounds.right, rtlBounds.right, 0.5f)
        compose.onNodeWithTag(tag).performTouchInput {
            val start = center
            down(start)
            advanceEventTime(650L)
            moveTo(start + Offset(48f, 0f))
            up()
        }
        compose.runOnIdle { assertTrue("Rightward drag must advance clip time", slideDelta > 0L) }
    }

    @Test fun ordinarySwipeBrowsesWithoutMovingTheSelectedClip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = EntryPointAccessors.fromApplication(context, TimelineTestDependencies::class.java).videoEngine()
        var scroll by mutableStateOf(1000L)
        var moveCount = 0
        val clip = Clip(id = "pan-clip", sourceUri = Uri.parse("content://test/audio"),
            sourceDurationMs = 8000L, timelineStartMs = 1500L, trimEndMs = 4000L)
        compose.setContent {
            ClearCutTheme {
                Timeline(
                    tracks = listOf(Track(id = "audio", type = TrackType.AUDIO, index = 0, clips = listOf(clip))),
                    playheadMs = 0, totalDurationMs = 12000, zoomLevel = 0.4f, scrollOffsetMs = scroll,
                    selectedClipId = clip.id, onClipSelected = { _, _ -> }, onPlayheadMoved = {},
                    onZoomChanged = {}, onScrollChanged = { scroll = it },
                    onSlideClip = { _, _ -> moveCount++ }, engine = engine, compactLayout = true,
                    modifier = Modifier.width(360.dp).height(320.dp),
                )
            }
        }
        compose.onNodeWithTag(ClearCutTestTags.TIMELINE_CLIP_PREFIX + clip.id).performTouchInput {
            swipe(center, center + Offset(48f, 0f), 300L)
        }
        compose.runOnIdle {
            assertEquals(0, moveCount)
            assertTrue("A normal swipe should pan", scroll < 1000L)
        }
    }

    @Test fun landscapeKeepsPreviewTimelineAndToolsInsideTheWorkspace() {
        compose.setContent {
            Box(Modifier.width(640.dp).height(320.dp).testTag("workspace")) {
                EditorPreviewTimelineWorkspace(
                    showPreview = true, immersivePreview = false, sideBySide = true,
                    previewMinHeight = 200.dp, timelineMinHeight = 240.dp, timelineMaxHeight = 360.dp,
                    preview = { Box(it.testTag("preview")) },
                    editing = { editingModifier, timelineModifier ->
                        Column(editingModifier) {
                            Box(timelineModifier.testTag("timeline"))
                            Box(Modifier.fillMaxWidth().height(64.dp).testTag("tools"))
                        }
                    },
                )
            }
        }
        val workspace = compose.onNodeWithTag("workspace").fetchSemanticsNode().boundsInRoot
        val preview = compose.onNodeWithTag("preview").fetchSemanticsNode().boundsInRoot
        val timeline = compose.onNodeWithTag("timeline").fetchSemanticsNode().boundsInRoot
        val tools = compose.onNodeWithTag("tools").fetchSemanticsNode().boundsInRoot
        assertTrue(preview.width > 0f && preview.height > 0f)
        assertTrue(timeline.width > 0f && timeline.height > 0f)
        assertTrue(preview.right <= timeline.left)
        assertTrue(timeline.bottom <= tools.top)
        assertTrue(tools.bottom <= workspace.bottom + 0.5f)
    }
}
