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

class AliyunOssService(private val httpClient: HttpClient) : StorageService {
    
    override suspend fun uploadPhoto(
        config: StorageConfig,
        photoData: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val objectKey = "photos/${TimeUtils.currentTimeMillis()}_$fileName"
            val url = "${config.endpoint}/${config.bucketName}/$objectKey"
            
            val date = TimeUtils.formatRfc1123()
            
            val signature = generateSignature(
                "PUT",
                "",
                mimeType,
                date,
                "/${config.bucketName}/$objectKey",
                config.accessKeySecret
            )
            
            val authHeader = "OSS ${config.accessKeyId}:$signature"
            
            val response = httpClient.put(url) {
                headers {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.Date, date)
                    append(HttpHeaders.Authorization, authHeader)
                }
                setBody(photoData)
            }
            
            if (response.status.isSuccess()) {
                Result.success(url)
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
            val objectKey = photoUrl.substringAfter("${config.endpoint}/${config.bucketName}/")
            val url = "${config.endpoint}/${config.bucketName}/$objectKey"
            
            val date = TimeUtils.formatRfc1123()
            
            val signature = generateSignature(
                "DELETE",
                "",
                "",
                date,
                "/${config.bucketName}/$objectKey",
                config.accessKeySecret
            )
            
            val authHeader = "OSS ${config.accessKeyId}:$signature"
            
            val response = httpClient.delete(url) {
                headers {
                    append(HttpHeaders.Date, date)
                    append(HttpHeaders.Authorization, authHeader)
                }
            }
            
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
            // OSS list objects API
            val url = "${config.endpoint}/"
            val queryParams = buildString {
                append("prefix=photos/")
                if (prefix.isNotEmpty()) {
                    append("&prefix=photos/$prefix")
                }
            }
            
            val date = TimeUtils.formatRfc1123()
            
            val signature = generateSignature(
                "GET",
                "",
                "",
                date,
                "/${config.bucketName}/?$queryParams",
                config.accessKeySecret
            )
            
            val authHeader = "OSS ${config.accessKeyId}:$signature"
            
            val response = httpClient.get("$url?$queryParams") {
                headers {
                    append(HttpHeaders.Date, date)
                    append(HttpHeaders.Authorization, authHeader)
                }
            }
            
            if (response.status.isSuccess()) {
                // Parse XML response to extract object keys
                // Simplified: return empty list for now
                Result.success(emptyList())
            } else {
                Result.failure(Exception("List failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateSignature(
        method: String,
        contentMd5: String,
        contentType: String,
        date: String,
        canonicalizedResource: String,
        secretKey: String
    ): String {
        val stringToSign = "$method\n$contentMd5\n$contentType\n$date\n$canonicalizedResource"
        // Note: HmacSHA1 requires platform-specific implementation
        // For now, return empty string - this needs to be implemented properly
        // In production, use a multiplatform crypto library
        return ""
    }
}

