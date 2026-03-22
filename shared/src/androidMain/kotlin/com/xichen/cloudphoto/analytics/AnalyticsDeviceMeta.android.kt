package com.xichen.cloudphoto.analytics

import android.content.Context
import android.os.Build

actual fun analyticsDeviceMeta(context: Any?): AnalyticsDeviceMeta {
    val ctx = (context as? Context)?.applicationContext
    val appVersion = ctx?.let { c ->
        runCatching {
            val pm = c.packageManager
            val pn = c.packageName
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pn, 0).versionName
        }.getOrNull()
    } ?: "unknown"
    return AnalyticsDeviceMeta(
        platform = "Android",
        appVersion = appVersion,
        osVersion = Build.VERSION.RELEASE ?: "unknown",
        deviceModel = Build.MODEL
    )
}
