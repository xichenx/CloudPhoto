package com.xichen.cloudphoto.core.logger

/**
 * 平台控制台输出（Logcat / NSLog），由 [CloudLoggingAntilog] 调用。
 */
internal expect fun platformConsoleLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
)
