package com.novacut.editor.ui.editor

internal const val TIMELINE_TOOLBAR_MIN_ZOOM = 0.01f
internal const val TIMELINE_TOOLBAR_MAX_ZOOM = 10f

internal object TimelineToolbarPolicy {
    fun clampZoom(zoomLevel: Float): Float =
        if (zoomLevel.isFinite()) {
            zoomLevel.coerceIn(TIMELINE_TOOLBAR_MIN_ZOOM, TIMELINE_TOOLBAR_MAX_ZOOM)
        } else {
            TIMELINE_TOOLBAR_MIN_ZOOM
        }

    fun zoomOut(zoomLevel: Float): Float =
        clampZoom(clampZoom(zoomLevel) * 0.75f)

    fun zoomIn(zoomLevel: Float): Float =
        clampZoom(clampZoom(zoomLevel) * 1.33f)
}
