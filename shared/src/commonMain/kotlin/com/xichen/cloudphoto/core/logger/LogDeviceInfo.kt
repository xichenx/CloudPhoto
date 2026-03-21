package com.xichen.cloudphoto.core.logger

/**
 * 随日志批次上报的设备与应用元信息。
 */
internal expect object LogDeviceInfo {
    fun platformName(): String
    fun osVersion(): String
    fun appVersion(): String
    fun deviceModel(): String?
}
