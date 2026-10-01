package com.novacut.editor.ui.projects

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.saveable.rememberSaveable
import com.novacut.editor.ui.theme.ClearCutPrimaryButton
import com.novacut.editor.ui.theme.WorkspaceDestinationRail
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novacut.editor.R
import com.novacut.editor.engine.UserTemplate
import com.novacut.editor.model.*
import com.novacut.editor.ui.ClearCutTestTags
import com.novacut.editor.ui.theme.ClearCutAccents
import com.novacut.editor.ui.theme.LocalClearCutColors
import com.novacut.editor.ui.theme.ClearCutChromeIconButton
import com.novacut.editor.ui.theme.ClearCutDialogIcon
import com.novacut.editor.ui.theme.ClearCutSecondaryButton
import com.novacut.editor.ui.theme.Radius
import com.novacut.editor.ui.theme.Spacing

data class ProjectTemplateUI(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val category: TemplateCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val aspectRatio: AspectRatio,
    val tracks: List<TrackType>,
    val suggestedDurationResId: Int
)

val projectTemplates = listOf(
    ProjectTemplateUI(
        id = "blank", nameResId = R.string.template_blank_name, descriptionResId = R.string.template_blank_desc,
        category = TemplateCategory.BLANK, icon = Icons.Default.Add, accentColor = ClearCutAccents.Neutral,
        aspectRatio = AspectRatio.RATIO_16_9, tracks = listOf(TrackType.VIDEO, TrackType.AUDIO),
        suggestedDurationResId = R.string.template_blank_duration
    ),
    ProjectTemplateUI(
        id = "vlog", nameResId = R.string.template_vlog_name, descriptionResId = R.string.template_vlog_desc,
        category = TemplateCategory.VLOG, icon = Icons.Default.Videocam, accentColor = ClearCutAccents.Mauve,
        aspectRatio = AspectRatio.RATIO_16_9,
        tracks = listOf(TrackType.VIDEO, TrackType.VIDEO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_vlog_duration
    ),
    ProjectTemplateUI(
        id = "tutorial", nameResId = R.string.template_tutorial_name, descriptionResId = R.string.template_tutorial_desc,
        category = TemplateCategory.TUTORIAL, icon = Icons.Default.School, accentColor = ClearCutAccents.Blue,
        aspectRatio = AspectRatio.RATIO_16_9,
        tracks = listOf(TrackType.VIDEO, TrackType.OVERLAY, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_tutorial_duration
    ),
    ProjectTemplateUI(
        id = "short_tiktok", nameResId = R.string.template_short_tiktok_name, descriptionResId = R.string.template_short_tiktok_desc,
        category = TemplateCategory.SHORT_FORM, icon = Icons.Default.PhoneAndroid, accentColor = ClearCutAccents.Red,
        aspectRatio = AspectRatio.RATIO_9_16,
        tracks = listOf(TrackType.VIDEO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_short_tiktok_duration
    ),
    ProjectTemplateUI(
        id = "short_reel", nameResId = R.string.template_reel_name, descriptionResId = R.string.template_reel_desc,
        category = TemplateCategory.SHORT_FORM, icon = Icons.Default.CameraRoll, accentColor = ClearCutAccents.Peach,
        aspectRatio = AspectRatio.RATIO_9_16,
        tracks = listOf(TrackType.VIDEO, TrackType.AUDIO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_reel_duration
    ),
    ProjectTemplateUI(
        id = "cinematic", nameResId = R.string.template_cinematic_name, descriptionResId = R.string.template_cinematic_desc,
        category = TemplateCategory.CINEMATIC, icon = Icons.Default.Movie, accentColor = ClearCutAccents.Yellow,
        aspectRatio = AspectRatio.RATIO_21_9,
        tracks = listOf(TrackType.VIDEO, TrackType.VIDEO, TrackType.AUDIO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_cinematic_duration
    ),
    ProjectTemplateUI(
        id = "slideshow", nameResId = R.string.template_slideshow_name, descriptionResId = R.string.template_slideshow_desc,
        category = TemplateCategory.SLIDESHOW, icon = Icons.Default.PhotoLibrary, accentColor = ClearCutAccents.Green,
        aspectRatio = AspectRatio.RATIO_16_9,
        tracks = listOf(TrackType.VIDEO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_slideshow_duration
    ),
    ProjectTemplateUI(
        id = "promo", nameResId = R.string.template_promo_name, descriptionResId = R.string.template_promo_desc,
        category = TemplateCategory.PROMO, icon = Icons.Default.Campaign, accentColor = ClearCutAccents.Teal,
        aspectRatio = AspectRatio.RATIO_16_9,
        tracks = listOf(TrackType.VIDEO, TrackType.OVERLAY, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_promo_duration
    ),
    ProjectTemplateUI(
        id = "square_social", nameResId = R.string.template_square_name, descriptionResId = R.string.template_square_desc,
        category = TemplateCategory.PROMO, icon = Icons.Default.CropSquare, accentColor = ClearCutAccents.Blue,
        aspectRatio = AspectRatio.RATIO_1_1,
        tracks = listOf(TrackType.VIDEO, TrackType.AUDIO, TrackType.TEXT),
        suggestedDurationResId = R.string.template_square_duration
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectTemplateSheet(
    onTemplateSelected: (ProjectTemplateUI, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onUserTemplateSelected: (UserTemplate) -> Unit = {},
    onDeleteUserTemplate: (String) -> Unit = {},
    onShareTemplate: (String) -> Unit = {},
    onImportTemplate: () -> Unit = {},
    userTemplates: List<UserTemplate> = emptyList(),
) {
    val colors = LocalClearCutColors.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    BackHandler(onBack = onDismiss)
    var destination by rememberSaveable { mutableStateOf("project_start") }
    var projectName by rememberSaveable { mutableStateOf("") }
    var canvas by rememberSaveable { mutableStateOf(AspectRatio.RATIO_16_9) }
    var aspectFilter by rememberSaveable { mutableStateOf<AspectRatio?>(null) }
    var pendingDeleteTemplate by remember { mutableStateOf<UserTemplate?>(null) }
    val untitledName = stringResource(R.string.project_untitled)
    val blankTemplate = projectTemplates.first { it.id == "blank" }
    val filteredTemplates = remember(aspectFilter) {
        projectTemplates.filter { aspectFilter == null || it.aspectRatio == aspectFilter }
    }
    val gridState = rememberLazyGridState()
    LaunchedEffect(destination, aspectFilter) {
        focusManager.clearFocus()
        keyboard?.hide()
        gridState.scrollToItem(0)
    }

    Column(
        modifier = modifier.fillMaxWidth().fillMaxHeight(0.92f)
            .testTag(ClearCutTestTags.TEMPLATE_SHEET)
            .background(colors.panel, RoundedCornerShape(topStart = Radius.xxl, topEnd = Radius.xxl))
            .navigationBarsPadding().imePadding().padding(horizontal = Spacing.lg, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.template_new_project), color = colors.text,
                    style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.template_workspace_subtitle), color = colors.subtext,
                    style = MaterialTheme.typography.bodySmall)
            }
            ClearCutChromeIconButton(
                icon = Icons.Default.Close, contentDescription = stringResource(R.string.close),
                onClick = onDismiss, containerColor = Color.Transparent, borderColor = Color.Transparent,
            )
        }
        Spacer(Modifier.height(12.dp))
        WorkspaceDestinationRail(
            destinations = listOf(
                "project_start" to stringResource(R.string.template_start_tab),
                "project_builtin" to stringResource(R.string.template_builtin_tab),
                "project_saved" to stringResource(R.string.template_saved_tab),
            ), selectedKey = destination, onSelected = { destination = it },
        )
        Spacer(Modifier.height(12.dp))
        // One scroll owner for the whole destination; no grids nested inside a scroll column.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp), state = gridState,
            modifier = Modifier.weight(1f).fillMaxWidth().testTag(ClearCutTestTags.TEMPLATE_GRID),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            when (destination) {
                "project_start" -> {
                    item(key = "project_basics", span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = projectName, onValueChange = { projectName = it }, singleLine = true,
                                label = { Text(stringResource(R.string.template_project_name)) },
                                placeholder = { Text(untitledName) },
                                shape = RoundedCornerShape(Radius.md),
                                modifier = Modifier.fillMaxWidth().testTag("new_project_name"),
                            )
                            Text(stringResource(R.string.template_canvas), style = MaterialTheme.typography.titleSmall,
                                color = colors.text)
                            TemplateCanvasPreview(canvas, Icons.Default.Movie, colors.accent)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AspectRatio.entries.forEach { ratio ->
                                    FilterChip(
                                        selected = canvas == ratio, onClick = { canvas = ratio },
                                        label = { Text(ratio.label) },
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("project_canvas_${ratio.name}"),
                                    )
                                }
                            }
                            ClearCutPrimaryButton(
                                text = stringResource(R.string.template_create_project), icon = Icons.Default.Add,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                    onTemplateSelected(blankTemplate.copy(aspectRatio = canvas),
                                        projectName.trim().ifBlank { untitledName })
                                }, modifier = Modifier.fillMaxWidth().testTag(ClearCutTestTags.TEMPLATE_BLANK),
                            )
                        }
                    }
                    item(key = "start_library", span = { GridItemSpan(maxLineSpan) }) {
                        TemplateLibraryEntry(
                            title = stringResource(R.string.template_built_in_section),
                            detail = stringResource(R.string.projects_templates_count, projectTemplates.size),
                            icon = Icons.Default.DashboardCustomize,
                            onClick = { destination = "project_builtin" },
                        )
                    }
                    item(key = "start_import", span = { GridItemSpan(maxLineSpan) }) {
                        ClearCutSecondaryButton(
                            text = stringResource(R.string.template_import), icon = Icons.Default.FileOpen,
                            onClick = onImportTemplate, modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                "project_builtin" -> {
                    item(key = "built_in_filters", span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.template_built_in_description), color = colors.subtext,
                                style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = aspectFilter == null, onClick = { aspectFilter = null },
                                    label = { Text(stringResource(R.string.template_filter_all)) })
                                projectTemplates.map { it.aspectRatio }.distinct().forEach { ratio ->
                                    FilterChip(selected = aspectFilter == ratio, onClick = { aspectFilter = ratio },
                                        label = { Text(ratio.label) },
                                        modifier = Modifier.testTag("template_filter_${ratio.name}"))
                                }
                            }
                        }
                    }
                    items(filteredTemplates, key = { "builtin_${it.id}" }) { template ->
                        ProjectTemplateCard(template) { name ->
                            onTemplateSelected(template, if (template.id == "blank") untitledName else name)
                        }
                    }
                }
                "project_saved" -> {
                    item(key = "saved_import", span = { GridItemSpan(maxLineSpan) }) {
                        ClearCutSecondaryButton(text = stringResource(R.string.template_import),
                            icon = Icons.Default.FileOpen, onClick = onImportTemplate, modifier = Modifier.fillMaxWidth())
                    }
                    if (userTemplates.isEmpty()) {
                        item(key = "saved_empty", span = { GridItemSpan(maxLineSpan) }) {
                            Column(Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.template_saved_empty_title), color = colors.text,
                                    style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.template_saved_empty_body), color = colors.subtext,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(userTemplates, key = { "saved_${it.id}" }) { template ->
                            UserTemplateCard(template, onClick = { onUserTemplateSelected(template) },
                                onDelete = { pendingDeleteTemplate = template }, onShare = { onShareTemplate(template.id) })
                        }
                    }
                }
            }
        }
    }
    pendingDeleteTemplate?.let { template ->
        DeleteUserTemplateDialog(template.name, onDismissRequest = { pendingDeleteTemplate = null },
            onConfirm = { pendingDeleteTemplate = null; onDeleteUserTemplate(template.id) })
    }
}

@Composable
private fun TemplateLibraryEntry(title: String, detail: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = LocalClearCutColors.current
    Surface(onClick = onClick, color = colors.panelHighest, shape = RoundedCornerShape(Radius.lg)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = colors.accent)
            Column(Modifier.weight(1f)) {
                Text(title, color = colors.text, style = MaterialTheme.typography.titleSmall)
                Text(detail, color = colors.subtext, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.subtext)
        }
    }
}

/** A schematic of the template's canvas, rather than a fake media thumbnail. */
@Composable
private fun TemplateCanvasPreview(ratio: AspectRatio, icon: ImageVector, accent: Color) {
    val colors = LocalClearCutColors.current
    val aspect = ratio.widthRatio.toFloat() / ratio.heightRatio
    val frameWidth = minOf(120f, 72f * aspect)
    Box(Modifier.fillMaxWidth().height(100.dp).background(colors.background, RoundedCornerShape(Radius.md)),
        contentAlignment = Alignment.Center) {
        Box(Modifier.size(frameWidth.dp, (frameWidth / aspect).dp)
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ProjectTemplateCard(template: ProjectTemplateUI, onClick: (String) -> Unit) {
    val colors = LocalClearCutColors.current
    val name = stringResource(template.nameResId)
    val description = stringResource(R.string.template_builtin_card_cd, name, formatCategory(template.category), template.aspectRatio.label)
    Surface(
        onClick = { onClick(name) }, color = colors.panelHighest, shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(1.dp, colors.cardStroke),
        modifier = Modifier.fillMaxWidth().testTag(if (template.id == "blank") ClearCutTestTags.TEMPLATE_BLANK else "template_builtin_${template.id}")
            .semantics { contentDescription = description },
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TemplateCanvasPreview(template.aspectRatio, template.icon, colors.accent)
            Text(name, color = colors.text, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(stringResource(template.descriptionResId), color = colors.subtext,
                style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(template.aspectRatio.label + " · " + stringResource(R.string.template_tracks_format, template.tracks.size),
                color = colors.accent, style = MaterialTheme.typography.labelSmall)
            Text(stringResource(template.suggestedDurationResId), color = colors.subtext, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun UserTemplateCard(template: UserTemplate, onClick: () -> Unit, onDelete: () -> Unit, onShare: () -> Unit) {
    val colors = LocalClearCutColors.current
    var showActions by remember { mutableStateOf(false) }
    Surface(onClick = onClick, color = colors.panelHighest, shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(1.dp, colors.cardStroke), modifier = Modifier.fillMaxWidth()
            .semantics { contentDescription = template.name + ", " + template.aspectRatio.label }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TemplateCanvasPreview(template.aspectRatio, Icons.Default.Bookmark, colors.accent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(template.name, modifier = Modifier.weight(1f), color = colors.text,
                    style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Box {
                    ClearCutChromeIconButton(icon = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.template_actions_format, template.name),
                        onClick = { showActions = true }, containerColor = Color.Transparent, borderColor = Color.Transparent)
                    DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.share)) },
                            onClick = { showActions = false; onShare() },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) })
                        DropdownMenuItem(text = { Text(stringResource(R.string.template_delete_confirm_action)) },
                            onClick = { showActions = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ClearCutAccents.Red) })
                    }
                }
            }
            Text(if (template.textOverlayCount > 0) stringResource(R.string.template_tracks_texts_format, template.trackTypes.size, template.textOverlayCount)
                else stringResource(R.string.template_tracks_format, template.trackTypes.size), color = colors.subtext, style = MaterialTheme.typography.bodySmall)
            Text(template.aspectRatio.label, color = colors.accent, style = MaterialTheme.typography.labelSmall)
            if (template.compatibility.slotCount > 0) {
                Text(stringResource(R.string.template_slots_format, template.compatibility.slotCount),
                    color = colors.subtext, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DeleteUserTemplateDialog(
    templateName: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            ClearCutDialogIcon(
                icon = Icons.Default.Delete,
                accent = ClearCutAccents.Red
            )
        },
        title = {
            Text(
                text = stringResource(R.string.template_delete_confirm_title),
                color = LocalClearCutColors.current.text,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.template_delete_confirm_body, templateName),
                color = LocalClearCutColors.current.subtext,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            ClearCutSecondaryButton(
                text = stringResource(R.string.template_delete_confirm_action),
                onClick = onConfirm,
                icon = Icons.Default.Delete,
                contentColor = ClearCutAccents.Red
            )
        },
        dismissButton = {
            ClearCutSecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismissRequest
            )
        },
        containerColor = LocalClearCutColors.current.panelHighest,
        titleContentColor = LocalClearCutColors.current.text,
        textContentColor = LocalClearCutColors.current.subtext,
        shape = RoundedCornerShape(Radius.xxl)
    )
}

private fun formatCategory(category: TemplateCategory): String {
    return category.name
        .replace('_', ' ')
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
