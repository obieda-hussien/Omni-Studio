package com.novacut.editor.ui.editor

/** Short, wide windows need two columns; stacking cannot preserve both editing and preview. */
internal fun useSideBySideEditor(widthDp: Int, heightDp: Int): Boolean =
    widthDp >= 640 && heightDp < 480 && widthDp > heightDp
