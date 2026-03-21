package com.xichen.cloudphoto.core.logger

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

internal actual object LogDeviceInfo {
    actual fun platformName(): String = "ios"

    actual fun osVersion(): String =
        UIDevice.currentDevice.systemVersion

    actual fun appVersion(): String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")?.toString() ?: "unknown"

    actual fun deviceModel(): String? =
        UIDevice.currentDevice.model
}
