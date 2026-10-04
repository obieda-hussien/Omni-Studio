package com.novacut.editor.ui.editor

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.novacut.editor.R
import com.novacut.editor.ui.theme.ClearCutTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AiToolsBrowserInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun searchSurvivesModelTabAndOpeningDetailsDoesNotStartProcessing() {
        val runs = mutableListOf<String>()
        render(onRun = runs::add)
        val captions = text(R.string.ai_tool_auto_captions)
        compose.onNodeWithTag("ai-tools-search").performTextInput(captions)
        compose.onNodeWithText(captions).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(emptyList<String>(), runs) }
        compose.onNodeWithText(text(R.string.ai_tools_tab_models)).performClick()
        compose.onNodeWithText(text(R.string.ai_tools_tab_tools)).performClick()
        compose.onNodeWithTag("ai-tools-search").assertTextContains(captions)
        compose.onNodeWithTag("ai-tool-action-auto_captions").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("auto_captions"), runs) }
    }

    @Test fun clipRequiredActionExplainsSelectionWithoutRunningTool() {
        val runs = mutableListOf<String>()
        val selectionHints = mutableListOf<String>()
        render(selected = false, onRun = runs::add, onSelectClip = selectionHints::add)
        val captions = text(R.string.ai_tool_auto_captions)
        compose.onNodeWithText(captions).performScrollTo().performClick()
        compose.onNodeWithTag("ai-tool-action-auto_captions").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(emptyList<String>(), runs)
            assertEquals(listOf(captions), selectionHints)
        }
    }

    @Test fun activeTaskPreventsAnotherRunAndCancelStaysAvailableAcrossTabs() {
        var cancels = 0
        render(processing = "auto_captions", onCancel = { cancels++ })
        compose.onNodeWithText(text(R.string.ai_tool_cut_assistant)).performScrollTo().performClick()
        compose.onNodeWithTag("ai-tool-action-cut_assistant").assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.ai_tools_tab_models)).performClick()
        compose.onNodeWithText(text(R.string.cancel)).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, cancels) }
    }

    @Test fun offlineModelsCannotStartDownloads() {
        var downloads = 0
        render(network = false, onDownload = { downloads++ })
        compose.onNodeWithText(text(R.string.ai_tools_tab_models)).performClick()
        val downloadButtons = compose.onAllNodesWithText(text(R.string.ai_tools_download)).fetchSemanticsNodes()
        assertEquals(5, downloadButtons.size)
        downloadButtons.indices.forEach { index ->
            compose.onAllNodesWithText(text(R.string.ai_tools_download))[index].assertIsNotEnabled()
        }
        compose.runOnIdle { assertEquals(0, downloads) }
    }

    private fun text(resource: Int) = compose.activity.getString(resource)

    private fun render(
        selected: Boolean = true,
        processing: String? = null,
        network: Boolean = true,
        onRun: (String) -> Unit = {},
        onSelectClip: (String) -> Unit = {},
        onCancel: () -> Unit = {},
        onDownload: () -> Unit = {},
    ) {
        compose.setContent {
            ClearCutTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    AiToolsPanel(
                        hasSelectedClip = selected,
                        onToolSelected = onRun,
                        onDisabledToolTapped = onSelectClip,
                        onClose = {},
                        processingTool = processing,
                        onCancelProcessing = onCancel,
                        networkAvailable = network,
                        onDownloadWhisper = onDownload,
                        onDownloadSegmentation = onDownload,
                        onDownloadInpainting = onDownload,
                    )
                }
            }
        }
    }
}
