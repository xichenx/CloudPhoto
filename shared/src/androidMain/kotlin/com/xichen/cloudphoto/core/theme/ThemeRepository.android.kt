package com.xichen.cloudphoto.core.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class ThemeRepository {
    private var context: Context? = null
    private val json = Json { ignoreUnknownKeys = true }

    actual fun init(context: Any?) {
        this.context = (context as? Context)?.applicationContext
    }
    
    private val prefs: SharedPreferences?
        get() = context?.getSharedPreferences("app_theme", Context.MODE_PRIVATE)
    
    actual suspend fun getThemeMode(): ThemeMode = withContext(Dispatchers.IO) {
        val prefs = prefs ?: return@withContext ThemeMode.SYSTEM
        val modeString = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    
    actual suspend fun setThemeMode(mode: ThemeMode): Unit = withContext(Dispatchers.IO) {
        prefs?.edit()?.putString("theme_mode", mode.name)?.apply() ?: Unit
    }
}

