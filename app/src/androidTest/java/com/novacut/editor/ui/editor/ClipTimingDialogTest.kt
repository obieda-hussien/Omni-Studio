package com.novacut.editor.ui.editor

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.novacut.editor.R
import com.novacut.editor.model.Clip
import com.novacut.editor.ui.theme.ClearCutTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClipTimingDialogTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val clip = Clip(id = "photo", sourceUri = Uri.parse("content://test/photo"),
        sourceDurationMs = 3000, timelineStartMs = 2000, isStillImage = true)

    @Test fun exactPositionAndArabicDurationAreAppliedTogether() {
        val results = mutableListOf<Pair<Long, Long>>()
        compose.setContent { ClearCutTheme { ClipTimingDialog(clip, { start, duration -> results += start to duration }, {}) } }
        compose.onNodeWithTag("clip-timing-start").performTextReplacement("5")
        compose.onNodeWithTag("clip-timing-duration").performTextReplacement("٨٫٠٠٠")
        compose.onNodeWithText(compose.activity.getString(R.string.timeline_apply_timing)).performClick()
        compose.runOnIdle { assertEquals(listOf(5000L to 8000L), results) }
    }

    @Test fun invalidDurationCannotBeApplied() {
        compose.setContent { ClearCutTheme { ClipTimingDialog(clip, { _, _ -> error("Invalid timing was applied") }, {}) } }
        compose.onNodeWithTag("clip-timing-duration").performTextReplacement("-8")
        compose.onNodeWithText(compose.activity.getString(R.string.timeline_apply_timing)).assertIsNotEnabled()
    }
}
