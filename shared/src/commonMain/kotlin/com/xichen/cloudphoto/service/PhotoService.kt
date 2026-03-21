package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.model.MockPhotos
import com.xichen.cloudphoto.model.Photo
import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.repository.ConfigRepository
import com.xichen.cloudphoto.repository.PhotoRepository
import com.xichen.cloudphoto.storage.StorageService
import com.xichen.cloudphoto.storage.StorageServiceFactory
import com.xichen.cloudphoto.util.TimeUtils
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class PhotoService(
    private val photoRepository: PhotoRepository,
    private val configRepository: ConfigRepository,
    private val httpClient: HttpClient
) {
    
    suspend fun uploadPhoto(
        photoData: ByteArray,
        fileName: String,
        mimeType: String,
        width: Int,
        height: Int,
        configId: String? = null,
        albumId: String? = null
    ): Result<Photo> = withContext(Dispatchers.Default) {
        try {
            val config = if (configId != null) {
                configRepository.getConfig(configId)
            } else {
                configRepository.getDefaultConfig()
            } ?: return@withContext Result.failure(Exception("No storage config available"))
            
            val storageService = StorageServiceFactory.create(config.provider, httpClient)
            
            val uploadResult = storageService.uploadPhoto(
                config = config,
                photoData = photoData,
                fileName = fileName,
                mimeType = mimeType
            )
            
            uploadResult.fold(
                onSuccess = { url ->
                    val photo = Photo(
                        id = generateId(),
                        name = fileName,
                        url = url,
                        size = photoData.size.toLong(),
                        width = width,
                        height = height,
                        mimeType = mimeType,
                        createdAt = Clock.System.now(),
                        albumId = albumId,
                        storageConfigId = config.id
                    )
                    
                    photoRepository.savePhoto(photo)
                    Result.success(photo)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePhoto(photoId: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val photo = photoRepository.getPhoto(photoId)
                ?: return@withContext Result.failure(Exception("Photo not found"))
            
            val config = configRepository.getConfig(photo.storageConfigId)
                ?: return@withContext Result.failure(Exception("Storage config not found"))
            
            val storageService = StorageServiceFactory.create(config.provider, httpClient)
            
            val deleteResult = storageService.deletePhoto(config, photo.url)
            
            deleteResult.fold(
                onSuccess = {
                    photoRepository.deletePhoto(photoId)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadPhoto(photoId: String): Result<ByteArray> = withContext(Dispatchers.Default) {
        try {
            val photo = photoRepository.getPhoto(photoId)
                ?: return@withContext Result.failure(Exception("Photo not found"))
            
            val config = configRepository.getConfig(photo.storageConfigId)
                ?: return@withContext Result.failure(Exception("Storage config not found"))
            
            val storageService = StorageServiceFactory.create(config.provider, httpClient)
            storageService.downloadPhoto(config, photo.url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 上传照片（失败时抛异常），便于 Swift 等平台使用 try/catch，无需处理 Kotlin Result。
     */
    suspend fun uploadPhotoThrowing(
        photoData: ByteArray,
        fileName: String,
        mimeType: String,
        width: Int,
        height: Int,
        configId: String? = null,
        albumId: String? = null
    ): Photo {
        return uploadPhoto(photoData, fileName, mimeType, width, height, configId, albumId)
            .fold(onSuccess = { it }, onFailure = { throw it })
    }

    /**
     * 删除照片（失败时抛异常），便于 Swift 等平台使用 try/catch。
     */
    suspend fun deletePhotoThrowing(photoId: String) {
        deletePhoto(photoId).fold(onSuccess = {}, onFailure = { throw it })
    }

    suspend fun getAllPhotos(): List<Photo> {
        val photos = photoRepository.getAllPhotos()
        return if (photos.isEmpty()) MockPhotos.tenFujiGrid() else photos
    }
    
    suspend fun getPhotosByAlbum(albumId: String): List<Photo> {
        return photoRepository.getPhotosByAlbum(albumId)
    }
    
    private fun generateId(): String {
        return "${TimeUtils.currentTimeMillis()}_${(0..999999).random()}"
    }
}

