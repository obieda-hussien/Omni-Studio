package com.novacut.editor.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** A compact destination switcher with growing labels and 48dp touch targets. */
@Composable
internal fun WorkspaceDestinationRail(
    destinations: List<Pair<String, String>>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClearCutColors.current
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup()
            .background(colors.panelRaised, RoundedCornerShape(Radius.lg)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        destinations.forEach { (key, label) ->
            val selected = selectedKey == key
            val background by animateColorAsState(
                if (selected) colors.selectedSurface else Color.Transparent,
                tween(Motion.DurationFast), label = "workspaceDestination",
            )
            Box(
                modifier = Modifier.weight(1f).heightIn(min = TouchTarget.minimum)
                    .background(background, RoundedCornerShape(Radius.md))
                    .selectable(selected, role = Role.Tab, onClick = { onSelected(key) })
                    .testTag(key).padding(horizontal = 6.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        if (colors.highContrast) colors.onAccent else colors.accent
                    } else colors.subtext,
                    textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
