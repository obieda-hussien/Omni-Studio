package com.novacut.editor.ui.editor

import java.util.Locale

internal enum class AiToolCategory {
    ALL, EDIT, AUDIO, BACKGROUND, MOTION, ENHANCE;
}

internal fun aiToolCategory(toolId: String): AiToolCategory = when (toolId) {
    "cut_assistant", "scene_detect" -> AiToolCategory.EDIT
    "auto_captions", "denoise" -> AiToolCategory.AUDIO
    "remove_bg", "bg_replace", "ai_background" -> AiToolCategory.BACKGROUND
    "track_motion", "face_track", "smart_crop", "stabilize", "ai_stabilize", "frame_interp" -> AiToolCategory.MOTION
    else -> AiToolCategory.ENHANCE
}

/** Search the localized copy, requiring every word; independent of the device's locale. */
internal fun matchesAiToolQuery(query: String, vararg text: String): Boolean {
    val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter(String::isNotEmpty)
    val searchable = text.joinToString(" ").lowercase(Locale.ROOT)
    return words.all(searchable::contains)
}
