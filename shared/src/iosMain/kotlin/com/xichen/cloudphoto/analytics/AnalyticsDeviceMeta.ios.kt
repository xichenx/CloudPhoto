package com.xichen.cloudphoto.analytics

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual fun analyticsDeviceMeta(context: Any?): AnalyticsDeviceMeta {
    val ver = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")?.toString() ?: "unknown"
    return AnalyticsDeviceMeta(
        platform = "iOS",
        appVersion = ver,
        osVersion = UIDevice.currentDevice.systemVersion,
        deviceModel = UIDevice.currentDevice.model
    )
}
