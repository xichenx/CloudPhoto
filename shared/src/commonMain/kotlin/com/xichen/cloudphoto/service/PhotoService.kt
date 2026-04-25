package com.xichen.cloudphoto.service

import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.model.Photo
import com.xichen.cloudphoto.model.toPhoto
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
    private val httpClient: HttpClient,
    private val photoApiService: PhotoApiService
) {

    companion object {
        private const val TIMELINE_PAGE_SIZE = 50
        private const val TIMELINE_MAX_PAGES = 200
    }
    
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
            if (photo != null) {
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
            } else {
                when (val apiResult = photoApiService.deletePhoto(photoId)) {
                    is ApiResult.Success -> Result.success(Unit)
                    is ApiResult.Error -> Result.failure(apiResult.exception)
                    is ApiResult.Loading -> Result.failure(Exception("Unexpected loading state"))
                }
            }
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

    /**
     * 本地缓存中的照片（直传/历史本地写入），不再混入 mock。
     */
    suspend fun getAllPhotos(): List<Photo> {
        return photoRepository.getAllPhotos()
    }

    /**
     * 从业务后端分页拉取当前用户云端照片时间线；未登录或请求失败时返回空列表。
     */
    suspend fun fetchTimelineFromCloud(): List<Photo> = withContext(Dispatchers.Default) {
        val accumulated = mutableListOf<Photo>()
        var page = 1
        while (page <= TIMELINE_MAX_PAGES) {
            when (val result = photoApiService.getPhotos(albumId = null, page = page, size = TIMELINE_PAGE_SIZE)) {
                is ApiResult.Success -> {
                    val pageDto = result.data
                    val batch = pageDto.records.map { it.toPhoto() }
                    if (batch.isEmpty()) {
                        break
                    }
                    accumulated.addAll(batch)
                    val totalPages = pageDto.pages
                    if (totalPages != null && totalPages > 0L && page.toLong() >= totalPages) {
                        break
                    }
                    if (batch.size < TIMELINE_PAGE_SIZE) {
                        break
                    }
                    page++
                }
                is ApiResult.Error -> break
                is ApiResult.Loading -> break
            }
        }
        accumulated.sortedByDescending { it.createdAt }
    }
    
    suspend fun getPhotosByAlbum(albumId: String): List<Photo> {
        return photoRepository.getPhotosByAlbum(albumId)
    }
    
    private fun generateId(): String {
        return "${TimeUtils.currentTimeMillis()}_${(0..999999).random()}"
    }
}

