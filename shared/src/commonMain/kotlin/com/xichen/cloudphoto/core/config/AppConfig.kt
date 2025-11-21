package com.xichen.cloudphoto.core.config

/**
 * 接口地址等配置 - 共享层统一管理，各平台不再硬编码
 */
object ApiConfig {
    /** 认证/业务 API 基础地址（登录、注册、验证码等） */
    const val AUTH_BASE_URL: String = "http://192.168.0.112:8080"
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

