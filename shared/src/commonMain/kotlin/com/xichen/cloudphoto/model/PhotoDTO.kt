package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 照片信息 DTO（与后端 PhotoDTO 对应）
 */
@Serializable
data class PhotoDTO(
    val id: String,
    val userId: String? = null,
    val originalUrl: String,
    val thumbnailUrl: String? = null,
    val filename: String? = null,
    val sizeBytes: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val mimeType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long? = null,
    val takenAt: Long? = null,
    val isPublic: Int? = null,
    val tags: List<String>? = null
)

/**
 * 转换为本地 Photo 模型
 */
fun PhotoDTO.toPhoto(): Photo {
    return Photo(
        id = id,
        name = filename ?: "photo",
        url = originalUrl,
        thumbnailUrl = thumbnailUrl,
        size = sizeBytes ?: 0L,
        width = width ?: 0,
        height = height ?: 0,
        mimeType = mimeType ?: "image/jpeg",
        createdAt = createdAt?.let { 
            kotlinx.datetime.Instant.fromEpochMilliseconds(it) 
        } ?: kotlinx.datetime.Clock.System.now(),
        albumId = null, // 需要从相册关联中获取
        storageConfigId = "" // 后端不返回此字段
    )
}
