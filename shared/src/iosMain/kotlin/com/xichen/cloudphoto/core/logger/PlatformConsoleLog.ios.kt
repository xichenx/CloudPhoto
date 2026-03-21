package com.xichen.cloudphoto.core.logger

import platform.Foundation.NSLog

internal actual fun platformConsoleLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
) {
    NSLog("%@", "[${level.name.first()}][$tag] $message")
    throwable?.printStackTrace()
}
