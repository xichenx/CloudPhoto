package com.xichen.cloudphoto.core.logger

import android.util.Log as AndroidLog

internal actual fun platformConsoleLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
) {
    when (level) {
        LogLevel.VERBOSE ->
            if (throwable != null) AndroidLog.v(tag, message, throwable) else AndroidLog.v(tag, message)
        LogLevel.DEBUG ->
            if (throwable != null) AndroidLog.d(tag, message, throwable) else AndroidLog.d(tag, message)
        LogLevel.INFO ->
            if (throwable != null) AndroidLog.i(tag, message, throwable) else AndroidLog.i(tag, message)
        LogLevel.WARN ->
            if (throwable != null) AndroidLog.w(tag, message, throwable) else AndroidLog.w(tag, message)
        LogLevel.ERROR ->
            if (throwable != null) AndroidLog.e(tag, message, throwable) else AndroidLog.e(tag, message)
    }
}
