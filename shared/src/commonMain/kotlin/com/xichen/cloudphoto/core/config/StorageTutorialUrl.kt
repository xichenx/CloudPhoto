package com.xichen.cloudphoto.core.config

/**
 * 存储配置教程页 URL（与 [ApiConfig.STORAGE_CONFIG_TUTORIAL_URL] 同源）
 *
 * Android 直接使用 [ApiConfig.STORAGE_CONFIG_TUTORIAL_URL]；
 * iOS Swift 通过本函数读取，避免 Kotlin object 在 Swift 侧命名差异。
 */
fun storageConfigTutorialUrlString(): String = ApiConfig.STORAGE_CONFIG_TUTORIAL_URL
