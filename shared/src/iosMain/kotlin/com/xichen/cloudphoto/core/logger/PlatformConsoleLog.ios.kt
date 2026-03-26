package com.xichen.cloudphoto.core.logger

import platform.Foundation.NSLog
import platform.Foundation.NSString

/**
 * `NSLog` 是可变参数 Objective-C API：把 Kotlin [String] 直接当作 `%@` 传入时，桥接到 `NSString` 不可靠，
 * 容易在运行时触发 EXC_BREAKPOINT。应先转为 [NSString]（Kotlin/Native 对 Apple 目标的显式转换）。
 */
internal actual fun platformConsoleLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
) {
    val line = buildString {
        append('[')
        append(level.name.first())
        append("][")
        append(tag)
        append("] ")
        append(message)
        if (throwable != null) {
            append('\n')
            append(throwable.stackTraceToString())
        }
    }
    NSLog("%@", line as NSString)
}
