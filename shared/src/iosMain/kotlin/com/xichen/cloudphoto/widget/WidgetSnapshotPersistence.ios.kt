package com.xichen.cloudphoto.widget

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import platform.Foundation.NSFileManager
import platform.posix.fflush
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
internal actual fun persistWidgetSnapshot(payload: WidgetSnapshotPayload, platformContext: Any?) {
    val base = NSFileManager.defaultManager
        .containerURLForSecurityApplicationGroupIdentifier(WidgetContract.IOS_APP_GROUP_ID)
        ?.path
        ?: return
    val path = "$base/${WidgetContract.SNAPSHOT_FILE_NAME}"
    val text = Json.encodeToString(WidgetSnapshotPayload.serializer(), payload)
    writeUtf8File(path, text)
}

@OptIn(ExperimentalForeignApi::class)
private fun writeUtf8File(path: String, content: String) {
    val bytes = content.encodeToByteArray()
    val f = fopen(path, "wb") ?: return
    try {
        bytes.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, bytes.size.convert(), f)
        }
        fflush(f)
    } finally {
        fclose(f)
    }
}
