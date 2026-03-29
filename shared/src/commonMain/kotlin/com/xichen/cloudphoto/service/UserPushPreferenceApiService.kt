package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.core.network.get
import com.xichen.cloudphoto.core.network.put
import com.xichen.cloudphoto.model.ApiResponse
import com.xichen.cloudphoto.model.UserPushPreferenceDto
import com.xichen.cloudphoto.model.UserPushPreferenceUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody

/**
 * 用户云端推送开关（服务端是否向该账号下发推送），需登录态。
 */
class UserPushPreferenceApiService(
    private val httpClient: HttpClient,
) {

    suspend fun getPreference(): ApiResult<UserPushPreferenceDto> {
        return try {
            val response = httpClient.get<ApiResponse<UserPushPreferenceDto>>(ApiConfig.USER_PUSH_PREFERENCE_PATH)
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

    suspend fun updatePreference(pushEnabled: Boolean): ApiResult<UserPushPreferenceDto> {
        return try {
            val response = httpClient.put<ApiResponse<UserPushPreferenceDto>>(
                ApiConfig.USER_PUSH_PREFERENCE_PATH,
            ) {
                setBody(UserPushPreferenceUpdateRequest(pushEnabled = pushEnabled))
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

    /** 供 Swift 等调用方：`first` 为开关，`second` 为错误文案。 */
    suspend fun loadOutcome(): Pair<Boolean?, String?> {
        return when (val r = getPreference()) {
            is ApiResult.Success -> Pair(r.data.pushEnabled, null)
            is ApiResult.Error -> Pair(
                null,
                r.message ?: r.exception.message ?: "加载失败，请稍后重试",
            )
            is ApiResult.Loading -> Pair(null, "请求中")
        }
    }

    suspend fun updateOutcome(pushEnabled: Boolean): Pair<Boolean, String?> {
        return when (val r = updatePreference(pushEnabled)) {
            is ApiResult.Success -> Pair(true, null)
            is ApiResult.Error -> Pair(
                false,
                r.message ?: r.exception.message ?: "保存失败，请稍后重试",
            )
            is ApiResult.Loading -> Pair(false, "请求中")
        }
    }
}
