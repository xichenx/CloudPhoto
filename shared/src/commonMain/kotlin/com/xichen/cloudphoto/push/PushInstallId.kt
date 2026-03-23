package com.xichen.cloudphoto.push

/**
 * Stable per-install identifier for push registration rows (Android Context / iOS ignored).
 */
expect fun pushInstallId(appContext: Any?): String
