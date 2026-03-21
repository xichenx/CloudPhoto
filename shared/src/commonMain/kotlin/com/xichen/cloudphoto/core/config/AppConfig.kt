package com.xichen.cloudphoto.core.config

/**
 * 接口地址等配置 - 共享层统一管理，各平台不再硬编码
 */
object ApiConfig {
    /** 认证/业务 API 基础地址（登录、注册、验证码等） */
    // 方案1：使用 ADB 端口转发（推荐用于调试）
    // 在命令行执行: adb reverse tcp:8080 tcp:8080
    // 然后使用 127.0.0.1:8080
    const val AUTH_BASE_URL: String = "http://127.0.0.1:8080"

    /**
     * 存储空间配置教程 URL（各云厂商获取 AccessKey/Bucket 等）
     * Android：直接使用本常量；iOS：通过 [storageConfigTutorialUrlString] 读取（同源）
     */
    const val STORAGE_CONFIG_TUTORIAL_URL: String =
        "https://help.aliyun.com/document_detail/31883.html"

    /**
     * 客户端诊断日志批量上报（相对 [AUTH_BASE_URL]）。
     * 服务端建议：鉴权可选；落库 MySQL/Mongo/ClickHouse 后供后台检索。
     */
    const val CLIENT_LOG_BATCH_PATH: String = "/api/client-logs/batch"
}

/**
 * 应用环境
 */
enum class AppEnvironment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

/**
 * 应用配置
 */
data class AppConfig(
    val environment: AppEnvironment,
    val apiBaseUrl: String? = null,
    val enableLogging: Boolean = true,
    val enableCrashReporting: Boolean = false
)

/**
 * 配置管理器
 */
expect class ConfigManager {
    fun getConfig(): AppConfig
    fun isDebug(): Boolean
    fun isProduction(): Boolean
}

