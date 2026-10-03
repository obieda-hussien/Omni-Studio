package com.novacut.editor.ui.editor

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.novacut.editor.R
import com.novacut.editor.ui.theme.ClearCutAccents
import com.novacut.editor.ui.theme.ClearCutPrimaryButton
import com.novacut.editor.ui.theme.LocalClearCutColors
import com.novacut.editor.ui.theme.Motion
import com.novacut.editor.ui.theme.Radius
import kotlin.math.roundToInt

@Composable
internal fun AiToolsBrowser(
    hasSelectedClip: Boolean,
    processingTool: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    category: AiToolCategory,
    onCategoryChange: (AiToolCategory) -> Unit,
    expandedToolId: String?,
    onExpandTool: (String) -> Unit,
    onToolSelected: (String) -> Unit,
    onDisabledToolTapped: (String) -> Unit,
    onManageModels: () -> Unit,
) {
    val colors = LocalClearCutColors.current
    val focusManager = LocalFocusManager.current
    val matchingTools = aiTools.filter { tool ->
        val name = stringResource(tool.nameResId)
        val description = stringResource(tool.descriptionResId)
        val categoryLabel = stringResource(aiToolCategory(tool.id).labelResId())
        (category == AiToolCategory.ALL || aiToolCategory(tool.id) == category) &&
            matchesAiToolQuery(query, name, description, categoryLabel)
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.ai_tools_search)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ai_tools_clear_search))
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(Radius.lg),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth().testTag("ai-tools-search"),
    )
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AiToolCategory.entries.forEach { entry ->
            FilterChip(
                selected = category == entry,
                onClick = { onCategoryChange(entry) },
                label = { Text(stringResource(entry.labelResId())) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
    if (!hasSelectedClip) {
        Text(
            stringResource(R.string.ai_tools_selection_help),
            color = colors.subtext,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Text(
        pluralStringResource(R.plurals.ai_tools_result_count, matchingTools.size, matchingTools.size),
        color = colors.subtext,
        style = MaterialTheme.typography.labelMedium,
    )
    if (matchingTools.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.ai_tools_no_results), style = MaterialTheme.typography.titleMedium, color = colors.text)
            Text(stringResource(R.string.ai_tools_search_hint), style = MaterialTheme.typography.bodyMedium, color = colors.subtext)
            TextButton(onClick = {
                onQueryChange("")
                onCategoryChange(AiToolCategory.ALL)
                focusManager.clearFocus()
            }) { Text(stringResource(R.string.ai_tools_reset_filters)) }
        }
    }
    matchingTools.forEach { tool ->
        val toolLabel = stringResource(tool.nameResId)
        AiToolRow(
            tool = tool,
            clipAvailable = !tool.requiresClip || hasSelectedClip,
            expanded = expandedToolId == tool.id,
            isProcessing = processingTool == tool.id,
            anyProcessing = processingTool != null,
            onExpand = { onExpandTool(tool.id) },
            onRun = {
                focusManager.clearFocus()
                onToolSelected(tool.id)
            },
            onSelectClip = { onDisabledToolTapped(toolLabel) },
            onManageModels = onManageModels,
        )
    }
}

@StringRes
private fun AiToolCategory.labelResId(): Int = when (this) {
    AiToolCategory.ALL -> R.string.ai_tools_category_all
    AiToolCategory.EDIT -> R.string.ai_tools_category_edit
    AiToolCategory.AUDIO -> R.string.ai_tools_category_audio
    AiToolCategory.BACKGROUND -> R.string.ai_tools_category_background
    AiToolCategory.MOTION -> R.string.ai_tools_category_motion
    AiToolCategory.ENHANCE -> R.string.ai_tools_category_enhance
}

@Composable
private fun AiToolRow(
    tool: AiToolConfig,
    clipAvailable: Boolean,
    expanded: Boolean,
    isProcessing: Boolean,
    anyProcessing: Boolean,
    onExpand: () -> Unit,
    onRun: () -> Unit,
    onSelectClip: () -> Unit,
    onManageModels: () -> Unit,
) {
    val colors = LocalClearCutColors.current
    val expansionLabel = stringResource(if (expanded) R.string.ai_tools_details_expanded else R.string.ai_tools_details_collapsed)
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(Motion.DurationFast)),
        color = colors.panelHighest,
        shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(1.dp, if (expanded) colors.accent.copy(alpha = 0.5f) else colors.cardStroke),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onExpand)
                    .semantics { stateDescription = expansionLabel }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(color = tool.color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        if (isProcessing) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = tool.color, strokeWidth = 2.dp)
                        } else {
                            Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(tool.nameResId), color = colors.text, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(tool.descriptionResId), color = colors.subtext, style = MaterialTheme.typography.bodyMedium)
                    if (isProcessing || !clipAvailable) {
                        Text(
                            stringResource(if (isProcessing) R.string.ai_tool_status_running else R.string.ai_tools_select_clip),
                            color = if (isProcessing) colors.accent else colors.subtext,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = colors.subtext)
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HorizontalDivider(color = colors.cardStroke)
                    Text(
                        stringResource(if (clipAvailable) tool.readinessHintResId else R.string.ai_tool_locked_hint),
                        color = colors.subtext,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (anyProcessing && !isProcessing) {
                        Text(stringResource(R.string.ai_tools_busy_hint), color = colors.subtext, style = MaterialTheme.typography.bodySmall)
                    }
                    ClearCutPrimaryButton(
                        text = stringResource(if (clipAvailable) R.string.ai_tools_use_tool else R.string.ai_tools_select_clip),
                        onClick = if (clipAvailable) onRun else onSelectClip,
                        enabled = !anyProcessing,
                        modifier = Modifier.fillMaxWidth().testTag("ai-tool-action-${tool.id}"),
                    )
                    if (tool.readinessResId == R.string.ai_tool_status_whisper ||
                        tool.readinessResId == R.string.ai_tool_status_fallback ||
                        tool.readinessResId == R.string.ai_tool_status_model_gated
                    ) {
                        TextButton(onClick = onManageModels, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.ai_tools_manage_models))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AiProcessingStatus(toolId: String, progress: Float, onCancel: () -> Unit) {
    val colors = LocalClearCutColors.current
    val toolName = aiTools.find { it.id == toolId }?.let { stringResource(it.nameResId) } ?: toolId
    val fraction = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ai_processing_format, toolName), color = colors.text, style = MaterialTheme.typography.labelLarge)
                if (fraction > 0f) {
                    Text(stringResource(R.string.ai_model_download_percent, (fraction * 100).roundToInt()), color = colors.subtext, style = MaterialTheme.typography.labelSmall)
                }
            }
            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel), color = ClearCutAccents.Red) }
        }
        if (fraction > 0f) {
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth(), color = colors.accent)
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colors.accent)
        }
    }
}
