package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.logger.ClientLogBatchRequest
import com.xichen.cloudphoto.core.logger.ClientLogEntry
import com.xichen.cloudphoto.core.logger.LogDeviceInfo
import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.core.network.postUnit
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import kotlin.random.Random

/**
 * 客户端诊断日志批量上报（与业务 API 同域，可带 Bearer Token）。
 */
class ClientLogApiService(
    private val httpClient: HttpClient
) {

    suspend fun uploadBatch(entries: List<ClientLogEntry>): ApiResult<Unit> {
        if (entries.isEmpty()) return ApiResult.Success(Unit)
        val batch = ClientLogBatchRequest(
            batchId = newBatchId(),
            platform = LogDeviceInfo.platformName(),
            osVersion = LogDeviceInfo.osVersion(),
            appVersion = LogDeviceInfo.appVersion(),
            deviceModel = LogDeviceInfo.deviceModel(),
            entries = entries
        )
        return httpClient.postUnit(ApiConfig.CLIENT_LOG_BATCH_PATH) {
            setBody(batch)
        }
    }

    private fun newBatchId(): String =
        buildString(32) {
            repeat(32) { append(Random.nextInt(0, 16).toString(16)) }
        }
}
