package com.xichen.cloudphoto.push

import android.content.Context
import java.util.UUID

actual fun pushInstallId(appContext: Any?): String {
    val ctx = (appContext as? Context)?.applicationContext
        ?: error("pushInstallId requires Android Context")
    val prefs = ctx.getSharedPreferences("cloudphoto_push", Context.MODE_PRIVATE)
    val existing = prefs.getString("install_id", null)
    if (existing != null) {
        return existing
    }
    val id = UUID.randomUUID().toString()
    prefs.edit().putString("install_id", id).apply()
    return id
}
