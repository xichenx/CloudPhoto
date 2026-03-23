package com.xichen.cloudphoto.push

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

@Suppress("unused_parameter")
actual fun pushInstallId(appContext: Any?): String {
    val defaults = NSUserDefaults.standardUserDefaults
    val key = "cloudphoto_push_install_id"
    defaults.stringForKey(key)?.let { return it }
    val newId = NSUUID().UUIDString
    defaults.setObject(newId, key)
    return newId
}
