package com.xichen.cloudphoto.storage.impl

import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.storage.StorageService
import com.xichen.cloudphoto.util.TimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Qiniu Cloud Storage Service
 * 
 * Note: Qiniu uses Upload Token mechanism, which is typically handled by the backend.
 * This implementation provides basic functionality for direct upload scenarios.
 * For production use, prefer using the backend API (PhotoApiService.uploadPhotoPresign).
 */
class QiniuService(private val httpClient: HttpClient) : StorageService {
    
    override suspend fun uploadPhoto(
        config: StorageConfig,
        photoData: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val objectKey = "photos/${TimeUtils.currentTimeMillis()}_$fileName"
            
            // Qiniu upload URL format: https://upload.qiniup.com/ or custom endpoint
            val uploadEndpoint = config.endpoint.removeSuffix("/")
            val uploadUrl = if (uploadEndpoint.contains("upload")) {
                uploadEndpoint
            } else {
                // Default Qiniu upload endpoint
                "https://upload.qiniup.com"
            }
            
            // For Qiniu, we typically need an upload token from backend
            // This is a simplified implementation - in production, use backend API
            val url = "$uploadEndpoint/$objectKey"
            
            val response = httpClient.put(url) {
                headers {
                    append(HttpHeaders.ContentType, mimeType)
                }
                setBody(photoData)
            }
            
            if (response.status.isSuccess()) {
                // Default Qiniu domain format: https://{bucket}.qiniucs.com/{key}
                val finalUrl = "https://${config.bucketName}.qiniucs.com/$objectKey"
                Result.success(finalUrl)
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePhoto(
        config: StorageConfig,
        photoUrl: String
    ): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            // Qiniu delete requires special authentication
            // In production, this should be handled by backend API
            val response = httpClient.delete(photoUrl)
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadPhoto(
        config: StorageConfig,
        photoUrl: String
    ): Result<ByteArray> = withContext(Dispatchers.Default) {
        try {
            val response = httpClient.get(photoUrl)
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Download failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun listPhotos(
        config: StorageConfig,
        prefix: String
    ): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            // Qiniu list requires special API
            // In production, this should be handled by backend API
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
