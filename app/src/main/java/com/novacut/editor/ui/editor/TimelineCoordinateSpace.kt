package com.novacut.editor.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Time is a physical left-to-right axis, independent of the device's text direction. */
@Composable
internal fun TimelineCoordinateSpace(content: @Composable() () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)
}
