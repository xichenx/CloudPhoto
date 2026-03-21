package com.xichen.cloudphoto.core.logger

import kotlinx.serialization.Serializable

@Serializable
data class ClientLogEntry(
    val tsEpochMillis: Long,
    val level: String,
    val tag: String,
    val message: String,
    val stack: String? = null
)

@Serializable
data class ClientLogBatchRequest(
    val batchId: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val deviceModel: String? = null,
    val entries: List<ClientLogEntry>
)
