package com.novacut.editor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novacut.editor.R
import com.novacut.editor.model.Clip

@Composable
internal fun ClipTimingDialog(clip: Clip, onApply: (Long, Long) -> Unit, onDismiss: () -> Unit) {
    var start by rememberSaveable(clip.id) { mutableStateOf(formatClipSeconds(clip.timelineStartMs)) }
    var duration by rememberSaveable(clip.id) { mutableStateOf(formatClipSeconds(clip.durationMs)) }
    val startMs = parseTrimTime(start)
    val durationMs = parseTrimTime(duration)
    val valid = startMs != null && startMs in 0L..MAX_STILL_IMAGE_DURATION_MS &&
        durationMs != null && durationMs in MIN_TIMELINE_CLIP_DURATION_MS..MAX_STILL_IMAGE_DURATION_MS
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.timeline_edit_timing)) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.timeline_timing_help), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = start, onValueChange = { start = it }, singleLine = true,
                    label = { Text(stringResource(R.string.timeline_start_seconds)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("clip-timing-start"),
                )
                OutlinedTextField(
                    value = duration, onValueChange = { duration = it }, singleLine = true,
                    label = { Text(stringResource(R.string.timeline_duration_seconds)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("clip-timing-duration"),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onApply(startMs!!, durationMs!!); onDismiss() }) {
                Text(stringResource(R.string.timeline_apply_timing))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
