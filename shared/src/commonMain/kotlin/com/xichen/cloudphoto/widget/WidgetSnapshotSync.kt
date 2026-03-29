package com.xichen.cloudphoto.widget

import com.xichen.cloudphoto.model.Photo

/**
 * Entry point for publishing photo previews to platform widgets.
 * Call after photo list changes (load, upload, delete, logout) and when [isLoggedIn] changes.
 *
 * @param isLoggedIn When false, widgets show a sign-in state (items are ignored).
 * @param platformContext Android: pass [android.content.Context] (typically `applicationContext`);
 *   iOS: omit or pass `null`.
 */
object WidgetSnapshotSync {
    fun publishFromPhotos(photos: List<Photo>, isLoggedIn: Boolean, platformContext: Any? = null) {
        val payload = WidgetSnapshotBuilder.build(photos, isLoggedIn)
        runCatching { persistWidgetSnapshot(payload, platformContext) }
    }
}

internal expect fun persistWidgetSnapshot(payload: WidgetSnapshotPayload, platformContext: Any?)
