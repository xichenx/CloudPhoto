package com.xichen.cloudphoto.storage

import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.storage.impl.AliyunOssService
import com.xichen.cloudphoto.storage.impl.AwsS3Service
import com.xichen.cloudphoto.storage.impl.CustomS3Service
import com.xichen.cloudphoto.storage.impl.MinioService
import com.xichen.cloudphoto.storage.impl.QiniuService
import com.xichen.cloudphoto.storage.impl.TencentCosService
import io.ktor.client.HttpClient

interface StorageService {
    suspend fun uploadPhoto(
        config: StorageConfig,
        photoData: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<String> // Returns the URL of uploaded photo
    
    suspend fun deletePhoto(
        config: StorageConfig,
        photoUrl: String
    ): Result<Unit>
    
    suspend fun downloadPhoto(
        config: StorageConfig,
        photoUrl: String
    ): Result<ByteArray>
    
    suspend fun listPhotos(
        config: StorageConfig,
        prefix: String = ""
    ): Result<List<String>> // Returns list of photo URLs
}

// Factory to create storage service based on provider
object StorageServiceFactory {
    fun create(provider: com.xichen.cloudphoto.model.StorageProvider, httpClient: HttpClient): StorageService {
        return when (provider) {
            com.xichen.cloudphoto.model.StorageProvider.ALIYUN_OSS ->
                AliyunOssService(httpClient)
            com.xichen.cloudphoto.model.StorageProvider.AWS_S3 ->
                AwsS3Service(httpClient)
            com.xichen.cloudphoto.model.StorageProvider.TENCENT_COS ->
                TencentCosService(httpClient)
            com.xichen.cloudphoto.model.StorageProvider.MINIO ->
                MinioService(httpClient)
            com.xichen.cloudphoto.model.StorageProvider.QINIU ->
                QiniuService(httpClient)
            com.xichen.cloudphoto.model.StorageProvider.CUSTOM_S3 ->
                CustomS3Service(httpClient)
        }
    }
}

