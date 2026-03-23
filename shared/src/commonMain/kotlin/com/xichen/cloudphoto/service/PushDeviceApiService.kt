package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.core.network.delete
import com.xichen.cloudphoto.core.network.post
import com.xichen.cloudphoto.model.ApiResponse
import com.xichen.cloudphoto.model.PushDeviceRegisterResponseData
import com.xichen.cloudphoto.model.RegisterPushDeviceRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.encodeURLParameter

/**
 * Registers FCM/APNs tokens with CloudPhotoAPI.
 */
class PushDeviceApiService(
    private val httpClient: HttpClient,
) {

    suspend fun register(request: RegisterPushDeviceRequest): ApiResult<PushDeviceRegisterResponseData> {
        return try {
            val response = httpClient.post<ApiResponse<PushDeviceRegisterResponseData>>(ApiConfig.PUSH_DEVICES_PATH) {
                setBody(request)
            }
            when (response) {
                is ApiResult.Success -> {
                    val body = response.data
                    if (body.code == 200 && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        ApiResult.Error(
                            Exception(body.message),
                            body.message,
                        )
                    }
                }
                is ApiResult.Error -> response
                is ApiResult.Loading -> ApiResult.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            ApiResult.Error(e, e.message)
        }
    }

    suspend fun registerOutcome(request: RegisterPushDeviceRequest): Pair<Boolean, String?> {
        return when (val r = register(request)) {
            is ApiResult.Success -> Pair(true, null)
            is ApiResult.Error -> Pair(false, r.message ?: r.exception.message ?: "注册推送失败")
            is ApiResult.Loading -> Pair(false, "请求中")
        }
    }

    suspend fun unregister(deviceInstallId: String): ApiResult<Unit> {
        return try {
            val q = deviceInstallId.encodeURLParameter()
            val path = "${ApiConfig.PUSH_DEVICES_PATH}?deviceInstallId=$q"
            val response = httpClient.delete<ApiResponse<Unit>>(path)
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200) {
                        ApiResult.Success(Unit)
                    } else {
                        ApiResult.Error(
                            Exception(response.data.message),
                            response.data.message,
                        )
                    }
                }
                is ApiResult.Error -> response
                is ApiResult.Loading -> ApiResult.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            ApiResult.Error(e, e.message)
        }
    }

    suspend fun unregisterOutcome(deviceInstallId: String): Pair<Boolean, String?> {
        return when (val r = unregister(deviceInstallId)) {
            is ApiResult.Success -> Pair(true, null)
            is ApiResult.Error -> Pair(false, r.message ?: r.exception.message ?: "注销推送失败")
            is ApiResult.Loading -> Pair(false, "请求中")
        }
    }
}
