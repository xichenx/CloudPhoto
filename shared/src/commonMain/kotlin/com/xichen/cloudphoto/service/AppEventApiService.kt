package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.core.network.post
import com.xichen.cloudphoto.model.ApiResponse
import com.xichen.cloudphoto.model.AppEventBatchReportDto
import com.xichen.cloudphoto.model.AppEventReportDto
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody

/**
 * App 埋点上报（需已配置 Bearer Token 的 [HttpClient]）。
 */
class AppEventApiService(
    private val httpClient: HttpClient
) {

    suspend fun reportEvent(event: AppEventReportDto): ApiResult<Unit> {
        return try {
            val response = httpClient.post<ApiResponse<Unit>>(ApiConfig.APP_EVENTS_PATH) {
                setBody(event)
            }
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200) {
                        ApiResult.Success(Unit)
                    } else {
                        ApiResult.Error(
                            Exception(response.data.message),
                            response.data.message
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

    suspend fun reportBatch(events: List<AppEventReportDto>): ApiResult<Unit> {
        if (events.isEmpty()) return ApiResult.Success(Unit)
        return try {
            val response = httpClient.post<ApiResponse<Unit>>(ApiConfig.APP_EVENTS_BATCH_PATH) {
                setBody(AppEventBatchReportDto(events = events))
            }
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200) {
                        ApiResult.Success(Unit)
                    } else {
                        ApiResult.Error(
                            Exception(response.data.message),
                            response.data.message
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
}
