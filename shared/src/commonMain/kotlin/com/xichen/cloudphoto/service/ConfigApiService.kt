package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.network.*
import com.xichen.cloudphoto.model.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * 对象存储配置 API 服务（调用后端 API；[HttpClient] 需已在 defaultRequest 中配置 Token）。
 */
class ConfigApiService(
    private val httpClient: HttpClient
) {
    
    /**
     * 获取配置列表
     */
    suspend fun getConfigs(): ApiResult<List<CloudConfigDTO>> {
        return try {
            val response = httpClient.get<ApiResponse<List<CloudConfigDTO>>>("/api/cloud-configs")
            
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200 && response.data.data != null) {
                        ApiResult.Success(response.data.data)
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
    
    /**
     * 获取配置详情
     */
    suspend fun getConfig(configId: String): ApiResult<CloudConfigDTO> {
        return try {
            val response = httpClient.get<ApiResponse<CloudConfigDTO>>("/api/cloud-configs/$configId")
            
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200 && response.data.data != null) {
                        ApiResult.Success(response.data.data)
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
    
    /**
     * 保存配置（创建或更新）
     */
    suspend fun saveConfig(config: CloudConfigDTO): ApiResult<CloudConfigDTO> {
        return try {
            val response = httpClient.post<ApiResponse<CloudConfigDTO>>("/api/cloud-configs") {
                setBody(config)
            }
            
            when (response) {
                is ApiResult.Success -> {
                    if (response.data.code == 200 && response.data.data != null) {
                        ApiResult.Success(response.data.data)
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
    
    /**
     * 激活配置（设为默认）
     */
    suspend fun activateConfig(configId: String): ApiResult<Unit> {
        return try {
            val response = httpClient.post<ApiResponse<Unit>>("/api/cloud-configs/$configId/activate")
            
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
    
    /**
     * 删除配置
     */
    suspend fun deleteConfig(configId: String): ApiResult<Unit> {
        return try {
            val response = httpClient.delete<ApiResponse<Unit>>("/api/cloud-configs/$configId")
            
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
    
    /**
     * 测试配置（验证配置是否有效）
     */
    suspend fun testConfig(configId: String): ApiResult<Unit> {
        return try {
            val response = httpClient.post<ApiResponse<Unit>>("/api/cloud-configs/$configId/test")
            
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
