package com.novacut.editor.ui.editor

import java.util.Locale
import kotlin.math.roundToLong

internal fun formatClipSeconds(ms: Long): String = String.format(Locale.US, "%.3f", ms / 1000.0)

internal fun formatTrimTime(ms: Long): String {
    val totalSeconds = ms / 1000.0
    val minutes = (totalSeconds / 60).toLong()
    return if (minutes > 0) String.format(Locale.US, "%d:%06.3f", minutes, totalSeconds % 60)
    else formatClipSeconds(ms)
}

/** Accept decimal seconds or mm:ss, including the numerals of an Arabic keyboard. */
internal fun parseTrimTime(text: String): Long? {
    val cleaned = text.trim().replace(',', '.').replace('٫', '.').map { char ->
        char.digitToIntOrNull()?.digitToChar() ?: char
    }.joinToString("")
    val parts = cleaned.split(':')
    val seconds = when (parts.size) {
        1 -> parts[0].toDoubleOrNull() ?: return null
        2 -> {
            val minutes = parts[0].toLongOrNull() ?: return null
            val secondsPart = parts[1].toDoubleOrNull() ?: return null
            if (minutes < 0 || !secondsPart.isFinite() || secondsPart !in 0.0..<60.0) return null
            minutes.toDouble() * 60 + secondsPart
        }
        else -> return null
    }
    if (!seconds.isFinite() || seconds < 0 || seconds >= Long.MAX_VALUE / 1000.0) return null
    return (seconds * 1000).roundToLong()
}
