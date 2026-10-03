package com.novacut.editor.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

/**
 * The phone editor gives the preview the flexible column slot. The timeline is
 * deliberately bounded so a large track stack cannot push the tool rail away.
 * Keeping these modifiers in the production layout seam lets Compose tests
 * exercise the actual sizing contract without reading EditorScreen.kt.
 */
internal fun ColumnScope.editorPreviewModifier(
    immersivePreview: Boolean,
    previewMinHeight: Dp,
): Modifier = if (immersivePreview) {
    Modifier.fillMaxSize()
} else {
    Modifier
        .fillMaxWidth()
        .weight(1f)
        .heightIn(min = previewMinHeight)
}

internal fun Modifier.editorTimelineModifier(
    timelineMinHeight: Dp,
    timelineMaxHeight: Dp,
): Modifier = fillMaxWidth().heightIn(
    min = timelineMinHeight,
    max = timelineMaxHeight,
)

@Composable
internal fun EditorPreviewTimelineWorkspace(
    showPreview: Boolean,
    immersivePreview: Boolean,
    sideBySide: Boolean,
    previewMinHeight: Dp,
    timelineMinHeight: Dp,
    timelineMaxHeight: Dp,
    modifier: Modifier = Modifier,
    preview: @Composable() (Modifier) -> Unit,
    editing: @Composable() (Modifier, Modifier) -> Unit,
) {
    if (sideBySide && showPreview && !immersivePreview) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            preview(Modifier.weight(0.38f).fillMaxHeight())
            editing(Modifier.weight(0.62f).fillMaxHeight(), Modifier.fillMaxWidth().weight(1f))
        }
    } else {
        Column(modifier = modifier) {
            if (showPreview) preview(editorPreviewModifier(immersivePreview, previewMinHeight))
            if (!immersivePreview) editing(
                Modifier.fillMaxWidth(),
                Modifier.editorTimelineModifier(timelineMinHeight, timelineMaxHeight),
            )
        }
    }
}
