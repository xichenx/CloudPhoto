package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.network.*
import com.xichen.cloudphoto.model.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.serialization.Serializable

/**
 * 照片 API 服务（调用后端 API；[HttpClient] 需已在 defaultRequest 中配置 Token）。
 */
class PhotoApiService(
    private val httpClient: HttpClient
) {
    
    /**
     * 获取照片列表
     */
    suspend fun getPhotos(
        albumId: String? = null,
        page: Int = 1,
        size: Int = 20
    ): ApiResult<PageDTO<PhotoDTO>> {
        return try {
            val queryParams = buildString {
                append("?page=$page&size=$size")
                albumId?.let { append("&albumId=$it") }
            }
            val response = httpClient.get<ApiResponse<PageDTO<PhotoDTO>>>("/api/photos$queryParams")
            
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
     * 获取单张照片详情
     */
    suspend fun getPhoto(photoId: String): ApiResult<PhotoDTO> {
        return try {
            val response = httpClient.get<ApiResponse<PhotoDTO>>("/api/photos/$photoId")
            
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
     * 上传照片（后端中转）
     */
    suspend fun uploadPhoto(
        photoData: ByteArray,
        fileName: String,
        contentType: String,
        albumId: String? = null,
        takenAt: Long? = null
    ): ApiResult<PhotoDTO> {
        return try {
            val response = httpClient.post<ApiResponse<PhotoDTO>>("/api/photos/upload") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = photoData,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, contentType)
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"file\"; filename=\"$fileName\""
                                    )
                                }
                            )
                            albumId?.let { append("albumId", it) }
                            takenAt?.let { append("takenAt", it.toString()) }
                        }
                    )
                )
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
     * 请求生成 Presigned URL（前端直传）
     */
    suspend fun requestPresignUrl(
        filename: String,
        contentType: String,
        size: Long,
        uploadMode: String = "S3_PUT"
    ): ApiResult<PresignResponseDTO> {
        return try {
            val request = PresignRequestDTO(
                filename = filename,
                contentType = contentType,
                size = size,
                uploadMode = uploadMode
            )
            val response = httpClient.post<ApiResponse<PresignResponseDTO>>("/api/photos/presign") {
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
     * 上传完成通知（前端直传后调用）
     */
    suspend fun completeUpload(
        remotePath: String,
        thumbnailUrl: String? = null,
        size: Long,
        takenAt: Long? = null,
        albumIds: List<String>? = null
    ): ApiResult<PhotoDTO> {
        return try {
            val request = PhotoCompleteDTO(
                remotePath = remotePath,
                thumbnailUrl = thumbnailUrl,
                size = size,
                takenAt = takenAt,
                albumIds = albumIds
            )
            val response = httpClient.post<ApiResponse<PhotoDTO>>("/api/photos/complete") {
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
     * 删除照片
     */
    suspend fun deletePhoto(photoId: String, deleteRemote: Boolean = false): ApiResult<Unit> {
        return try {
            val queryParams = if (deleteRemote) "?deleteRemote=true" else ""
            val response = httpClient.delete<ApiResponse<Unit>>("/api/photos/$photoId$queryParams")
            
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
     * 批量删除照片
     */
    suspend fun batchDeletePhotos(photoIds: List<String>, deleteRemote: Boolean = false): ApiResult<Unit> {
        return try {
            val request = BatchDeleteDTO(ids = photoIds)
            val queryParams = if (deleteRemote) "?deleteRemote=true" else ""
            val response = httpClient.post<ApiResponse<Unit>>("/api/photos/batch-delete$queryParams") {
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
 * Presigned URL 请求 DTO
 */
@Serializable
data class PresignRequestDTO(
    val filename: String,
    val contentType: String,
    val size: Long,
    val uploadMode: String? = null
)

/**
 * Presigned URL 响应 DTO
 */
@Serializable
data class PresignResponseDTO(
    val url: String,
    val method: String? = null,
    val headers: Map<String, String>? = null,
    val remotePath: String,
    val expiresAt: Long? = null
)

/**
 * 照片上传完成 DTO
 */
@Serializable
data class PhotoCompleteDTO(
    val remotePath: String,
    val thumbnailUrl: String? = null,
    val size: Long,
    val takenAt: Long? = null,
    val albumIds: List<String>? = null
)

/**
 * 批量删除 DTO
 */
@Serializable
data class BatchDeleteDTO(
    val ids: List<String>
)
