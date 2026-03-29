package com.xichen.cloudphoto.core.config

/**
 * 存储配置教程页 URL（与 [ApiConfig.STORAGE_CONFIG_TUTORIAL_URL] 同源）
 *
 * Android 直接使用 [ApiConfig]；iOS Swift 使用 [StorageTutorialUrls.value]，避免顶层函数在 Swift 侧 `*Kt` 命名不稳定。
 */
fun storageConfigTutorialUrlString(): String = ApiConfig.STORAGE_CONFIG_TUTORIAL_URL

object StorageTutorialUrls {
    fun value(): String = ApiConfig.STORAGE_CONFIG_TUTORIAL_URL
}
