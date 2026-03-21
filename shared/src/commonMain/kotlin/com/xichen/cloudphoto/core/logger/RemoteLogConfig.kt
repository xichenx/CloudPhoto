package com.xichen.cloudphoto.core.logger

/**
 * 诊断日志远程队列开关（默认开启；可在设置页关闭以节省流量）。
 */
object RemoteLogConfig {
    var bufferToFileEnabled: Boolean = true
}
