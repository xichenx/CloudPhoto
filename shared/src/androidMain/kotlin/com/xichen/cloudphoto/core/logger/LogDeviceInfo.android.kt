package com.xichen.cloudphoto.core.logger

import android.content.Context
import android.os.Build

private var appContext: Context? = null

internal fun bindAndroidLogDeviceContext(context: Context?) {
    appContext = context?.applicationContext
}

internal actual object LogDeviceInfo {
    actual fun platformName(): String = "android"

    actual fun osVersion(): String = Build.VERSION.RELEASE ?: "unknown"

    actual fun appVersion(): String {
        val ctx = appContext ?: return "unknown"
        return runCatching {
            val pm = ctx.packageManager
            val pn = ctx.packageName
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pn, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")
    }

    actual fun deviceModel(): String? = Build.MODEL
}
