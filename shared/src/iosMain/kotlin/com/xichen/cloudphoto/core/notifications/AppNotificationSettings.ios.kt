package com.xichen.cloudphoto.core.notifications

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Suppress("DEPRECATION")
actual object AppNotificationSettings {
    actual fun open(appContext: Any?) {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
