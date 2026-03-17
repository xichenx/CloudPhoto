package com.xichen.cloudphoto.core.theme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

actual class ThemeRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun init(context: Any?) {}

    actual suspend fun getThemeMode(): ThemeMode = withContext(Dispatchers.Default) {
        val modeString = userDefaults.stringForKey("theme_mode") ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    
    actual suspend fun setThemeMode(mode: ThemeMode) = withContext(Dispatchers.Default) {
        userDefaults.setObject(mode.name, forKey = "theme_mode")
    }
}

