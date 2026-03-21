package com.xichen.cloudphoto.core.logger

import io.github.aakira.napier.Napier

/**
 * 日志级别（业务侧与控制台一致）。
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * 安装 Napier 管道（控制台 + 可上传队列）。请在 Application / 首屏 ViewModel 中尽早调用一次。
 */
object DiagnosticLogging {
    private var installed: Boolean = false

    fun install(rootContext: Any?) {
        if (installed) return
        installed = true
        RemoteLogFileStore.init(rootContext)
        Napier.base(CloudLoggingAntilog())
    }

    fun isInstalled(): Boolean = installed
}

/**
 * 统一日志入口（基于 Napier → [CloudLoggingAntilog]）。
 */
object Log {
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        Napier.v(tag = tag, throwable = throwable, message = { message })
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Napier.d(tag = tag, throwable = throwable, message = { message })
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Napier.i(tag = tag, throwable = throwable, message = { message })
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Napier.w(tag = tag, throwable = throwable, message = { message })
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Napier.e(tag = tag, throwable = throwable, message = { message })
    }

    inline fun <reified T> T.logV(message: String, throwable: Throwable? = null) {
        v(T::class.simpleName ?: "Unknown", message, throwable)
    }

    inline fun <reified T> T.logD(message: String, throwable: Throwable? = null) {
        d(T::class.simpleName ?: "Unknown", message, throwable)
    }

    inline fun <reified T> T.logI(message: String, throwable: Throwable? = null) {
        i(T::class.simpleName ?: "Unknown", message, throwable)
    }

    inline fun <reified T> T.logW(message: String, throwable: Throwable? = null) {
        w(T::class.simpleName ?: "Unknown", message, throwable)
    }

    inline fun <reified T> T.logE(message: String, throwable: Throwable? = null) {
        e(T::class.simpleName ?: "Unknown", message, throwable)
    }
}
