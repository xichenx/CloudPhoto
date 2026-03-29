package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.core.network.post
import com.xichen.cloudphoto.model.ApiResponse
import com.xichen.cloudphoto.model.FeedbackSubmitRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody

/**
 * 用户意见反馈（需登录态，与相册等业务共用带 Token 的 [HttpClient]）。
 */
class FeedbackApiService(
    private val httpClient: HttpClient,
) {

    suspend fun submit(request: FeedbackSubmitRequest): ApiResult<Unit> {
        return try {
            val response = httpClient.post<ApiResponse<Unit>>(ApiConfig.FEEDBACK_PATH) {
                setBody(request)
            }
            when (response) {
                is ApiResult.Success -> {
                    val body = response.data
                    if (body.code == 200) {
                        ApiResult.Success(Unit)
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

    /** 供 Swift 等调用方使用的二元结果。 */
    suspend fun submitOutcome(content: String, category: String): Pair<Boolean, String?> {
        val trimmed = content.trim()
        if (trimmed.length < 5) {
            return Pair(false, "请至少输入 5 个字的反馈内容")
        }
        if (trimmed.length > 2000) {
            return Pair(false, "反馈内容请勿超过 2000 字")
        }
        val req = FeedbackSubmitRequest(
            content = trimmed,
            category = category,
        )
        return when (val r = submit(req)) {
            is ApiResult.Success -> Pair(true, null)
            is ApiResult.Error -> Pair(false, r.message ?: r.exception.message ?: "提交失败，请稍后重试")
            is ApiResult.Loading -> Pair(false, "请求中")
        }
    }
}
