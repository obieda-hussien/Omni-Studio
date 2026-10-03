package com.novacut.editor.ui.editor

import com.novacut.editor.ui.theme.ClearCutAccents
import com.novacut.editor.ui.theme.LocalClearCutColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novacut.editor.R
import com.novacut.editor.engine.InpaintingModelState
import com.novacut.editor.engine.StabilizationProfileValidation
import com.novacut.editor.engine.segmentation.SegmentationModelState
import com.novacut.editor.engine.whisper.WhisperModelState
import com.novacut.editor.ui.theme.ClearCutDialogIcon
import com.novacut.editor.ui.theme.ClearCutPrimaryButton
import com.novacut.editor.ui.theme.ClearCutSecondaryButton
import com.novacut.editor.ui.theme.Radius
import kotlin.math.roundToInt

data class AiToolConfig(
    val id: String,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    val icon: ImageVector,
    val color: Color,
    val requiresClip: Boolean = true,
    @StringRes val readinessResId: Int = R.string.ai_tool_status_ready,
    @StringRes val readinessHintResId: Int = R.string.ai_tool_ready_hint,
    val readinessAccent: Color = ClearCutAccents.Green
)

val aiTools = listOf(
    AiToolConfig(
        "cut_assistant",
        R.string.ai_tool_cut_assistant,
        R.string.ai_tool_cut_assistant_desc,
        Icons.Default.ContentCut,
        ClearCutAccents.Peach,
        requiresClip = false,
        readinessResId = R.string.ai_tool_status_review,
        readinessHintResId = R.string.ai_tool_hint_review,
        readinessAccent = ClearCutAccents.Peach
    ),
    AiToolConfig(
        "auto_captions",
        R.string.ai_tool_auto_captions,
        R.string.ai_tool_auto_captions_desc,
        Icons.Default.ClosedCaption,
        ClearCutAccents.Blue,
        readinessResId = R.string.ai_tool_status_whisper,
        readinessHintResId = R.string.ai_tool_hint_whisper_optional,
        readinessAccent = ClearCutAccents.Blue
    ),
    AiToolConfig(
        "remove_bg",
        R.string.ai_tool_remove_bg,
        R.string.ai_tool_remove_bg_desc,
        Icons.Default.Wallpaper,
        ClearCutAccents.Green,
        readinessResId = R.string.ai_tool_status_fallback,
        readinessHintResId = R.string.ai_tool_hint_segmentation_fallback,
        readinessAccent = ClearCutAccents.Teal
    ),
    AiToolConfig(
        "bg_replace",
        R.string.tool_replace_bg,
        R.string.ai_tool_bg_replace_desc,
        Icons.Default.PhotoFilter,
        ClearCutAccents.Lavender,
        readinessResId = R.string.ai_tool_status_model_gated,
        readinessHintResId = R.string.ai_tool_hint_model_required,
        readinessAccent = ClearCutAccents.Peach
    ),
    AiToolConfig(
        "scene_detect",
        R.string.ai_tool_scene_detect,
        R.string.ai_tool_scene_detect_desc,
        Icons.Default.ContentCut,
        ClearCutAccents.Peach
    ),
    AiToolConfig(
        "track_motion",
        R.string.ai_tool_track_motion,
        R.string.ai_tool_track_motion_desc,
        Icons.Default.GpsFixed,
        ClearCutAccents.Mauve
    ),
    AiToolConfig(
        "face_track",
        R.string.tool_face_track,
        R.string.ai_tool_face_track_desc,
        Icons.Default.Face,
        ClearCutAccents.Mauve
    ),
    AiToolConfig(
        "smart_crop",
        R.string.ai_tool_smart_crop,
        R.string.ai_tool_smart_crop_desc,
        Icons.Default.Crop,
        ClearCutAccents.Teal
    ),
    AiToolConfig(
        "auto_color",
        R.string.ai_tool_auto_color,
        R.string.ai_tool_auto_color_desc,
        Icons.Default.Palette,
        ClearCutAccents.Yellow
    ),
    AiToolConfig(
        "stabilize",
        R.string.ai_tool_stabilize,
        R.string.ai_tool_stabilize_desc,
        Icons.Default.Straighten,
        ClearCutAccents.Sapphire
    ),
    AiToolConfig(
        "denoise",
        R.string.ai_tool_denoise,
        R.string.ai_tool_denoise_desc,
        Icons.AutoMirrored.Filled.VolumeOff,
        ClearCutAccents.Flamingo
    ),
    AiToolConfig(
        "video_upscale",
        R.string.ai_tool_ai_upscale,
        R.string.ai_tool_ai_upscale_desc,
        Icons.Default.ZoomIn,
        ClearCutAccents.Rosewater,
        readinessResId = R.string.ai_tool_status_model_gated,
        readinessHintResId = R.string.ai_tool_hint_model_required,
        readinessAccent = ClearCutAccents.Peach
    ),
    AiToolConfig(
        "frame_interp",
        R.string.tool_frame_interp,
        R.string.ai_tool_frame_interp_desc,
        Icons.Default.SlowMotionVideo,
        ClearCutAccents.Sky,
        readinessResId = R.string.ai_tool_status_ready,
        readinessHintResId = R.string.ai_frame_interp_hint,
        readinessAccent = ClearCutAccents.Green
    ),
    AiToolConfig(
        "ai_background",
        R.string.ai_tool_ai_background,
        R.string.ai_tool_ai_background_desc,
        Icons.Default.PhotoFilter,
        ClearCutAccents.Lavender,
        readinessResId = R.string.ai_tool_status_model_gated,
        readinessHintResId = R.string.ai_tool_hint_model_required,
        readinessAccent = ClearCutAccents.Peach
    ),
    AiToolConfig(
        "ai_stabilize",
        R.string.ai_tool_ai_stabilize,
        R.string.ai_tool_ai_stabilize_desc,
        Icons.Default.Straighten,
        ClearCutAccents.Sky,
        readinessResId = R.string.ai_tool_status_ready,
        readinessHintResId = R.string.ai_tool_hint_stabilize_local,
        readinessAccent = ClearCutAccents.Teal
    ),
    AiToolConfig(
        "ai_style_transfer",
        R.string.ai_tool_style_transfer,
        R.string.ai_tool_style_transfer_desc,
        Icons.Default.Style,
        ClearCutAccents.Maroon,
        readinessResId = R.string.ai_tool_status_model_gated,
        readinessHintResId = R.string.ai_tool_hint_model_required,
        readinessAccent = ClearCutAccents.Peach
    )
)

private enum class AiModelRemovalTarget {
    WHISPER,
    SEGMENTATION,
    INPAINTING
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiToolsPanel(
    hasSelectedClip: Boolean,
    onToolSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDisabledToolTapped: (String) -> Unit = {},
    onCancelProcessing: () -> Unit = {},
    onClose: () -> Unit,
    processingTool: String? = null,
    processingProgress: Float = 0f,
    stabilizationPreview: StabilizationPreview? = null,
    onApplyStabilizationPreview: () -> Unit = {},
    onDismissStabilizationPreview: () -> Unit = {},
    stabilizationProfileImportPreview: StabilizationProfileValidation? = null,
    onImportStabilizationProfile: () -> Unit = {},
    onExportStabilizationProfile: () -> Unit = {},
    onApplyStabilizationProfileImport: () -> Unit = {},
    onDismissStabilizationProfileImport: () -> Unit = {},
    whisperModelState: WhisperModelState = WhisperModelState.NOT_DOWNLOADED,
    whisperDownloadProgress: Float = 0f,
    onDownloadWhisper: () -> Unit = {},
    onDeleteWhisper: () -> Unit = {},
    segmentationModelState: SegmentationModelState = SegmentationModelState.NOT_DOWNLOADED,
    segmentationDownloadProgress: Float = 0f,
    onDownloadSegmentation: () -> Unit = {},
    onDeleteSegmentation: () -> Unit = {},
    inpaintingModelState: InpaintingModelState = InpaintingModelState.NOT_DOWNLOADED,
    inpaintingDownloadProgress: Float = 0f,
    onDownloadInpainting: () -> Unit = {},
    onDeleteInpainting: () -> Unit = {},
    networkAvailable: Boolean = true,
) {
    val semanticColors = LocalClearCutColors.current
    var pendingModelRemoval by remember { mutableStateOf<AiModelRemovalTarget?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var expandedToolId by rememberSaveable { mutableStateOf<String?>(null) }
    val toolsScroll = rememberScrollState()
    val modelsScroll = rememberScrollState()
    val focusManager = LocalFocusManager.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth().imePadding()) {
        PremiumEditorPanel(
            title = stringResource(R.string.ai_tools_title),
            subtitle = stringResource(R.string.ai_tools_browser_subtitle),
            icon = Icons.Default.AutoAwesome,
            accent = ClearCutAccents.Mauve,
            onClose = onClose,
            modifier = Modifier.heightIn(max = maxHeight * 0.92f),
            scrollable = false,
        ) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                listOf(R.string.ai_tools_tab_tools, R.string.ai_tools_tab_models).forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            focusManager.clearFocus()
                            selectedTab = index
                        },
                        text = { Text(stringResource(title)) },
                    )
                }
            }
            if (processingTool != null) {
                AiProcessingStatus(
                    toolId = processingTool,
                    progress = processingProgress,
                    onCancel = onCancelProcessing,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(if (selectedTab == 0) toolsScroll else modelsScroll)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (selectedTab == 0) {
                    AiToolsBrowser(
                        hasSelectedClip = hasSelectedClip,
                        processingTool = processingTool,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        category = AiToolCategory.valueOf(selectedCategory),
                        onCategoryChange = { selectedCategory = it.name },
                        expandedToolId = expandedToolId,
                        onExpandTool = { expandedToolId = if (expandedToolId == it) null else it },
                        onToolSelected = onToolSelected,
                        onDisabledToolTapped = onDisabledToolTapped,
                        onManageModels = {
                            focusManager.clearFocus()
                            selectedTab = 1
                        },
                    )
                } else {
                    Text(
                        stringResource(R.string.ai_tools_models_description),
                        color = semanticColors.subtext,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    ModelStatusCard(
                        accent = modelAccent(whisperModelState),
                        icon = Icons.Default.RecordVoiceOver,
                        title = stringResource(R.string.ai_tools_model_speech),
                        status = when (whisperModelState) {
                            WhisperModelState.READY -> stringResource(R.string.ai_whisper_ready)
                            WhisperModelState.DOWNLOADING -> stringResource(R.string.ai_downloading_model)
                            WhisperModelState.ERROR -> stringResource(R.string.ai_download_failed)
                            else -> stringResource(R.string.ai_whisper_size)
                        },
                        description = when (whisperModelState) {
                            WhisperModelState.READY -> stringResource(R.string.ai_whisper_active)
                            WhisperModelState.NOT_DOWNLOADED -> stringResource(R.string.ai_whisper_description)
                            WhisperModelState.ERROR -> stringResource(R.string.ai_tools_speech_error)
                            WhisperModelState.DOWNLOADING -> stringResource(R.string.ai_tools_speech_downloading)
                        },
                        progress = if (whisperModelState == WhisperModelState.DOWNLOADING) whisperDownloadProgress else null,
                        primaryActionLabel = when (whisperModelState) {
                            WhisperModelState.NOT_DOWNLOADED, WhisperModelState.ERROR -> stringResource(R.string.ai_tools_download)
                            else -> null
                        },
                        onPrimaryAction = when (whisperModelState) {
                            WhisperModelState.NOT_DOWNLOADED, WhisperModelState.ERROR -> onDownloadWhisper
                            else -> null
                        },
                        networkAvailable = networkAvailable,
                        secondaryActionLabel = if (whisperModelState == WhisperModelState.READY) {
                            stringResource(R.string.ai_model_remove_action)
                        } else {
                            null
                        },
                        onSecondaryAction = if (whisperModelState == WhisperModelState.READY) {
                            { pendingModelRemoval = AiModelRemovalTarget.WHISPER }
                        } else {
                            null
                        }
                    )

                    ModelStatusCard(
                        accent = segmentationAccent(segmentationModelState),
                        icon = Icons.Default.PersonOff,
                        title = stringResource(R.string.ai_tools_model_background),
                        status = when (segmentationModelState) {
                            SegmentationModelState.READY -> stringResource(R.string.ai_segmentation_ready)
                            SegmentationModelState.DOWNLOADING -> stringResource(R.string.ai_downloading_model)
                            SegmentationModelState.ERROR -> stringResource(R.string.ai_download_failed)
                            else -> stringResource(R.string.ai_segmentation_size)
                        },
                        description = when (segmentationModelState) {
                            SegmentationModelState.READY -> stringResource(R.string.ai_tools_background_ready)
                            SegmentationModelState.NOT_DOWNLOADED -> stringResource(R.string.ai_segmentation_description)
                            SegmentationModelState.ERROR -> stringResource(R.string.ai_tools_background_error)
                            SegmentationModelState.DOWNLOADING -> stringResource(R.string.ai_tools_background_downloading)
                        },
                        progress = if (segmentationModelState == SegmentationModelState.DOWNLOADING) segmentationDownloadProgress else null,
                        primaryActionLabel = when (segmentationModelState) {
                            SegmentationModelState.NOT_DOWNLOADED, SegmentationModelState.ERROR -> stringResource(R.string.ai_tools_download)
                            else -> null
                        },
                        onPrimaryAction = when (segmentationModelState) {
                            SegmentationModelState.NOT_DOWNLOADED, SegmentationModelState.ERROR -> onDownloadSegmentation
                            else -> null
                        },
                        networkAvailable = networkAvailable,
                        secondaryActionLabel = if (segmentationModelState == SegmentationModelState.READY) {
                            stringResource(R.string.ai_model_remove_action)
                        } else {
                            null
                        },
                        onSecondaryAction = if (segmentationModelState == SegmentationModelState.READY) {
                            { pendingModelRemoval = AiModelRemovalTarget.SEGMENTATION }
                        } else {
                            null
                        }
                    )

                    ModelStatusCard(
                        accent = inpaintingAccent(inpaintingModelState),
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.ai_tools_model_objects),
                        status = when (inpaintingModelState) {
                            InpaintingModelState.READY -> stringResource(R.string.ai_inpainting_ready)
                            InpaintingModelState.DOWNLOADING -> stringResource(R.string.ai_downloading_model)
                            InpaintingModelState.ERROR -> stringResource(R.string.ai_download_failed)
                            InpaintingModelState.NOT_DOWNLOADED -> stringResource(R.string.ai_inpainting_size)
                        },
                        description = when (inpaintingModelState) {
                            InpaintingModelState.READY -> stringResource(R.string.ai_inpainting_description)
                            InpaintingModelState.NOT_DOWNLOADED -> stringResource(R.string.ai_inpainting_description)
                            InpaintingModelState.ERROR -> stringResource(R.string.ai_model_download_failed_toast)
                            InpaintingModelState.DOWNLOADING -> stringResource(R.string.ai_inpainting_description)
                        },
                        progress = if (inpaintingModelState == InpaintingModelState.DOWNLOADING) {
                            inpaintingDownloadProgress
                        } else {
                            null
                        },
                        primaryActionLabel = when (inpaintingModelState) {
                            InpaintingModelState.NOT_DOWNLOADED, InpaintingModelState.ERROR -> stringResource(R.string.ai_tools_download)
                            else -> null
                        },
                        onPrimaryAction = when (inpaintingModelState) {
                            InpaintingModelState.NOT_DOWNLOADED, InpaintingModelState.ERROR -> onDownloadInpainting
                            else -> null
                        },
                        networkAvailable = networkAvailable,
                        secondaryActionLabel = if (inpaintingModelState == InpaintingModelState.READY) {
                            stringResource(R.string.ai_model_remove_action)
                        } else {
                            null
                        },
                        onSecondaryAction = if (inpaintingModelState == InpaintingModelState.READY) {
                            { pendingModelRemoval = AiModelRemovalTarget.INPAINTING }
                        } else {
                            null
                        }
                    )

                    PremiumPanelCard(accent = ClearCutAccents.Sapphire) {
                        Text(
                            text = stringResource(R.string.ai_stabilization_profiles_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = semanticColors.text,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.ai_stabilization_profiles_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = semanticColors.subtext,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ClearCutSecondaryButton(
                                text = stringResource(R.string.ai_stabilization_profile_import),
                                onClick = onImportStabilizationProfile,
                                icon = Icons.Default.Download,
                                modifier = Modifier.weight(1f),
                            )
                            ClearCutSecondaryButton(
                                text = stringResource(R.string.ai_stabilization_profile_export),
                                onClick = onExportStabilizationProfile,
                                icon = Icons.Default.Upload,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                }
            }
        }
    }

    pendingModelRemoval?.let { target ->
        AiModelRemovalConfirmDialog(
            target = target,
            onDismissRequest = { pendingModelRemoval = null },
            onConfirm = {
                when (target) {
                    AiModelRemovalTarget.WHISPER -> onDeleteWhisper()
                    AiModelRemovalTarget.SEGMENTATION -> onDeleteSegmentation()
                    AiModelRemovalTarget.INPAINTING -> onDeleteInpainting()
                }
                pendingModelRemoval = null
            }
        )
    }

    stabilizationPreview?.let { preview ->
        StabilizationPreviewDialog(
            preview = preview,
            onDismissRequest = onDismissStabilizationPreview,
            onApply = onApplyStabilizationPreview,
        )
    }

    stabilizationProfileImportPreview?.let { validation ->
        StabilizationProfileImportDialog(
            validation = validation,
            onDismissRequest = onDismissStabilizationProfileImport,
            onApply = onApplyStabilizationProfileImport,
        )
    }
}

@Composable
private fun StabilizationPreviewDialog(
    preview: StabilizationPreview,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
) {
    val semanticColors = LocalClearCutColors.current
    val motionData = preview.motionData
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            ClearCutDialogIcon(
                icon = Icons.Default.Straighten,
                accent = ClearCutAccents.Sapphire,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.ai_stabilization_preview_title),
                color = semanticColors.text,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = pluralStringResource(
                    R.plurals.ai_stabilization_preview_body,
                    motionData.frameCount,
                    preview.sourceName,
                    motionData.frameCount,
                    motionData.averageShakeMagnitude * 100f,
                    (motionData.recommendedCropScale - 1f) * 100f,
                ),
                color = semanticColors.subtext,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            ClearCutPrimaryButton(
                text = stringResource(R.string.ai_stabilization_preview_apply),
                onClick = onApply,
                icon = Icons.Default.Straighten,
            )
        },
        dismissButton = {
            ClearCutSecondaryButton(
                text = stringResource(R.string.ai_stabilization_preview_cancel),
                onClick = onDismissRequest,
            )
        },
        containerColor = semanticColors.panelHighest,
        titleContentColor = semanticColors.text,
        textContentColor = semanticColors.subtext,
        shape = RoundedCornerShape(Radius.xxl),
    )
}

@Composable
private fun StabilizationProfileImportDialog(
    validation: StabilizationProfileValidation,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
) {
    val semanticColors = LocalClearCutColors.current
    val profile = validation.profile
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            ClearCutDialogIcon(
                icon = Icons.Default.Straighten,
                accent = if (profile != null) ClearCutAccents.Sapphire else ClearCutAccents.Red,
            )
        },
        title = {
            Text(
                text = stringResource(
                    if (profile != null) {
                        R.string.ai_stabilization_profile_preview_title
                    } else {
                        R.string.ai_stabilization_profile_import_failed
                    },
                ),
                color = semanticColors.text,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (profile != null) {
                        stringResource(
                            R.string.ai_stabilization_profile_preview_body,
                            profile.name,
                            profile.lens.name,
                            profile.motion.algorithm,
                            profile.cropScale,
                            profile.syncOffsetMs,
                        )
                    } else {
                        validation.warnings.firstOrNull()
                            ?: stringResource(R.string.ai_stabilization_profile_import_failed)
                    },
                    color = semanticColors.subtext,
                    style = MaterialTheme.typography.bodyMedium,
                )
                validation.warnings.forEach { warning ->
                    Text(
                        text = warning,
                        color = ClearCutAccents.Peach,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = stringResource(R.string.ai_stabilization_profile_reason_code, validation.reasonCode),
                    color = semanticColors.subtext,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        confirmButton = {
            if (profile != null) {
                ClearCutPrimaryButton(
                    text = stringResource(R.string.ai_stabilization_profile_activate),
                    onClick = onApply,
                    icon = Icons.Default.Straighten,
                )
            } else {
                ClearCutSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismissRequest,
                )
            }
        },
        dismissButton = if (profile != null) {
            {
                ClearCutSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismissRequest,
                )
            }
        } else {
            null
        },
        containerColor = semanticColors.panelHighest,
        titleContentColor = semanticColors.text,
        textContentColor = semanticColors.subtext,
        shape = RoundedCornerShape(Radius.xxl),
    )
}

@Composable
private fun AiModelRemovalConfirmDialog(
    target: AiModelRemovalTarget,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    val semanticColors = LocalClearCutColors.current
    val title = when (target) {
        AiModelRemovalTarget.WHISPER -> stringResource(R.string.ai_remove_whisper_title)
        AiModelRemovalTarget.SEGMENTATION -> stringResource(R.string.ai_remove_segmentation_title)
        AiModelRemovalTarget.INPAINTING -> stringResource(R.string.ai_remove_inpainting_title)
    }
    val body = when (target) {
        AiModelRemovalTarget.WHISPER -> stringResource(R.string.ai_remove_whisper_message)
        AiModelRemovalTarget.SEGMENTATION -> stringResource(R.string.ai_remove_segmentation_message)
        AiModelRemovalTarget.INPAINTING -> stringResource(R.string.ai_remove_inpainting_message)
    }

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
                text = title,
                color = semanticColors.text,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = body,
                color = semanticColors.subtext,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            ClearCutSecondaryButton(
                text = stringResource(R.string.ai_model_remove_confirm),
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
        containerColor = semanticColors.panelHighest,
        titleContentColor = semanticColors.text,
        textContentColor = semanticColors.subtext,
        shape = RoundedCornerShape(Radius.xxl)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelStatusCard(
    accent: Color,
    icon: ImageVector,
    title: String,
    status: String,
    description: String,
    progress: Float?,
    primaryActionLabel: String?,
    onPrimaryAction: (() -> Unit)?,
    secondaryActionLabel: String?,
    onSecondaryAction: (() -> Unit)?,
    networkAvailable: Boolean,
) {
    val semanticColors = LocalClearCutColors.current
    val hasPrimaryAction = primaryActionLabel != null && onPrimaryAction != null
    val hasSecondaryAction = secondaryActionLabel != null && onSecondaryAction != null
    val displayDescription = if (!networkAvailable && hasPrimaryAction) {
        "$description ${stringResource(R.string.ai_model_offline_description)}"
    } else {
        description
    }

    PremiumPanelCard(accent = accent) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = accent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(Radius.lg),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = semanticColors.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = semanticColors.subtext,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = semanticColors.subtext
                )
            }
        }

        if (progress != null) {
            val normalizedProgress = progress.coerceIn(0f, 1f)
            val progressPercent = (normalizedProgress * 100).roundToInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ai_model_download_progress),
                    style = MaterialTheme.typography.labelMedium,
                    color = semanticColors.subtext
                )
                Text(
                    text = stringResource(R.string.ai_model_download_percent, progressPercent),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }

            LinearProgressIndicator(
                progress = { normalizedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Radius.sm)),
                color = accent,
                trackColor = semanticColors.surface
            )
        }

        if (progress == null && (hasPrimaryAction || hasSecondaryAction)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasPrimaryAction) {
                    ClearCutPrimaryButton(
                        text = primaryActionLabel,
                        onClick = onPrimaryAction,
                        icon = Icons.Default.Download,
                        modifier = Modifier.widthIn(min = 112.dp),
                        enabled = networkAvailable,
                    )
                }

                if (hasSecondaryAction) {
                    ClearCutSecondaryButton(
                        text = secondaryActionLabel,
                        onClick = onSecondaryAction,
                        contentColor = ClearCutAccents.Red,
                        icon = Icons.Default.Delete,
                        modifier = Modifier.widthIn(min = 112.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun modelAccent(state: WhisperModelState): Color {
    val semanticColors = LocalClearCutColors.current
    return when (state) {
        WhisperModelState.READY -> ClearCutAccents.Blue
        WhisperModelState.DOWNLOADING -> ClearCutAccents.Yellow
        WhisperModelState.ERROR -> ClearCutAccents.Red
        WhisperModelState.NOT_DOWNLOADED -> semanticColors.surfaceHigh
    }
}

@Composable
private fun segmentationAccent(state: SegmentationModelState): Color {
    val semanticColors = LocalClearCutColors.current
    return when (state) {
        SegmentationModelState.READY -> ClearCutAccents.Green
        SegmentationModelState.DOWNLOADING -> ClearCutAccents.Yellow
        SegmentationModelState.ERROR -> ClearCutAccents.Red
        SegmentationModelState.NOT_DOWNLOADED -> semanticColors.surfaceHigh
    }
}

@Composable
private fun inpaintingAccent(state: InpaintingModelState): Color {
    val semanticColors = LocalClearCutColors.current
    return when (state) {
        InpaintingModelState.READY -> ClearCutAccents.Mauve
        InpaintingModelState.DOWNLOADING -> ClearCutAccents.Yellow
        InpaintingModelState.ERROR -> ClearCutAccents.Red
        InpaintingModelState.NOT_DOWNLOADED -> semanticColors.surfaceHigh
    }
}
