package com.xichen.cloudphoto.widget

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Writes [WidgetSnapshotPayload] JSON into app-internal storage.
 * The host app module (composeApp) is responsible for calling Glance `updateAll` after this.
 */
internal actual fun persistWidgetSnapshot(payload: WidgetSnapshotPayload, platformContext: Any?) {
    val context = platformContext as? Context ?: return
    val app = context.applicationContext
    val file = File(app.filesDir, WidgetContract.SNAPSHOT_FILE_NAME)
    file.writeText(Json.encodeToString(WidgetSnapshotPayload.serializer(), payload))
}
