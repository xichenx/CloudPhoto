package com.xichen.cloudphoto.core.theme

expect class ThemeRepository() {
    /**
     * 初始化（仅 Android 需要 Context；iOS 为 no-op）
     */
    fun init(context: Any?)

    suspend fun getThemeMode(): ThemeMode
    suspend fun setThemeMode(mode: ThemeMode)
}

