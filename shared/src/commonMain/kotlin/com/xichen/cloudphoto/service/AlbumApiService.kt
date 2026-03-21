package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.network.*
import com.xichen.cloudphoto.model.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

/**
 * 相册 API 服务（调用后端 API；[HttpClient] 需已在 defaultRequest 中配置 Token）。
 */
class AlbumApiService(
    private val httpClient: HttpClient
) {
    
    /**
     * 获取相册列表
     */
    suspend fun getAlbums(page: Int = 1, size: Int = 20): ApiResult<PageDTO<AlbumDTO>> {
        return try {
            val response = httpClient.get<ApiResponse<PageDTO<AlbumDTO>>>("/api/albums?page=$page&size=$size")
            
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
     * 获取相册详情
     */
    suspend fun getAlbum(albumId: String): ApiResult<AlbumDTO> {
        return try {
            val response = httpClient.get<ApiResponse<AlbumDTO>>("/api/albums/$albumId")
            
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
     * 创建相册
     */
    suspend fun createAlbum(name: String): ApiResult<AlbumDTO> {
        return try {
            val request = CreateAlbumDTO(name = name)
            val response = httpClient.post<ApiResponse<AlbumDTO>>("/api/albums") {
                setBody(request)
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
     * 更新相册
     */
    suspend fun updateAlbum(albumId: String, name: String? = null, coverPhotoId: String? = null): ApiResult<AlbumDTO> {
        return try {
            val request = UpdateAlbumDTO(name = name, coverPhotoId = coverPhotoId)
            val response = httpClient.put<ApiResponse<AlbumDTO>>("/api/albums/$albumId") {
                setBody(request)
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
     * 删除相册
     */
    suspend fun deleteAlbum(albumId: String): ApiResult<Unit> {
        return try {
            val response = httpClient.delete<ApiResponse<Unit>>("/api/albums/$albumId")
            
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
     * 添加照片到相册
     */
    suspend fun addPhotosToAlbum(albumId: String, photoIds: List<String>): ApiResult<Unit> {
        return try {
            val request = AddPhotosToAlbumDTO(photoIds = photoIds)
            val response = httpClient.post<ApiResponse<Unit>>("/api/albums/$albumId/photos") {
                setBody(request)
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
    
    /**
     * 从相册移除照片
     */
    suspend fun removePhotosFromAlbum(albumId: String, photoIds: List<String>): ApiResult<Unit> {
        return try {
            val request = AddPhotosToAlbumDTO(photoIds = photoIds)
            val response = httpClient.delete<ApiResponse<Unit>>("/api/albums/$albumId/photos") {
                setBody(request)
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

/**
 * 创建相册请求 DTO
 */
@Serializable
data class CreateAlbumDTO(
    val name: String
)

/**
 * 更新相册请求 DTO
 */
@Serializable
data class UpdateAlbumDTO(
    val name: String? = null,
    val coverPhotoId: String? = null
)

/**
 * 添加照片到相册请求 DTO
 */
@Serializable
data class AddPhotosToAlbumDTO(
    val photoIds: List<String>
)
