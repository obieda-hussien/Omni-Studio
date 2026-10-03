package com.novacut.editor.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.novacut.editor.ui.theme.ClearCutTheme

@Preview(name = "Phone", widthDp = 360, heightDp = 780)
@Preview(name = "Landscape phone", widthDp = 720, heightDp = 360)
@Preview(name = "Large text", widthDp = 360, heightDp = 780, fontScale = 1.6f)
@Composable
private fun AiToolsSelectedClipPreview() {
    ClearCutTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AiToolsPanel(hasSelectedClip = true, onToolSelected = {}, onClose = {})
        }
    }
}

@Preview(name = "Select a clip", widthDp = 360, heightDp = 780)
@Composable
private fun AiToolsNoSelectionPreview() {
    ClearCutTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AiToolsPanel(hasSelectedClip = false, onToolSelected = {}, onClose = {})
        }
    }
}

@Preview(name = "Processing", widthDp = 720, heightDp = 360)
@Composable
private fun AiToolsProcessingPreview() {
    ClearCutTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AiToolsPanel(
                hasSelectedClip = true,
                onToolSelected = {},
                onClose = {},
                processingTool = "auto_captions",
                processingProgress = 0.42f,
            )
        }
    }
}
