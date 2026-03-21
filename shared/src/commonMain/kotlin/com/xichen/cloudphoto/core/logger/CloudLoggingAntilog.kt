package com.xichen.cloudphoto.core.logger

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel as NapierLevel
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val storeJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
}

/**
 * Napier 出口：控制台 +（可选）本地持久化队列供批量上传。
 */
internal class CloudLoggingAntilog : Antilog() {

    override fun performLog(
        priority: NapierLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val safeTag = tag?.take(128) ?: "CloudPhoto"
        val rawMessage = message ?: ""
        val level = priority.toAppLevel()
        platformConsoleLog(level, safeTag, rawMessage, throwable)

        if (!RemoteLogConfig.bufferToFileEnabled) return

        val sanitizedMessage = LogSanitizer.sanitize(rawMessage)
        val stack = throwable?.let {
            LogSanitizer.sanitize(it.stackTraceToString().take(12_000))
        }
        val entry = ClientLogEntry(
            tsEpochMillis = Clock.System.now().toEpochMilliseconds(),
            level = level.name,
            tag = safeTag,
            message = sanitizedMessage,
            stack = stack
        )
        runCatching {
            RemoteLogFileStore.appendLine(storeJson.encodeToString(entry))
        }
    }

    private fun NapierLevel.toAppLevel(): LogLevel = when (this) {
        NapierLevel.VERBOSE -> LogLevel.VERBOSE
        NapierLevel.DEBUG -> LogLevel.DEBUG
        NapierLevel.INFO -> LogLevel.INFO
        NapierLevel.WARNING -> LogLevel.WARN
        NapierLevel.ERROR -> LogLevel.ERROR
        NapierLevel.ASSERT -> LogLevel.ERROR
    }
}
