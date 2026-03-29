package com.xichen.cloudphoto.core.logger

/**
 * 诊断日志配置。
 *
 * - [bufferToFileEnabled]：是否写入本地 NDJSON 队列（默认开启）。
 * - [periodicRemoteUploadEnabled]：是否后台定时上传；关闭后仅能通过「关于 → 上传日志」主动上传。
 */
object RemoteLogConfig {
    var bufferToFileEnabled: Boolean = true

    /** 默认关闭定时上传，避免静默消耗流量；与设置页「上传日志」配合使用。 */
    var periodicRemoteUploadEnabled: Boolean = false
}
