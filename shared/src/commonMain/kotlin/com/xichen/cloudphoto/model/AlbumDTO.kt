package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 相册信息 DTO（与后端 AlbumDTO 对应）
 */
@Serializable
data class AlbumDTO(
    val id: String,
    val userId: String? = null,
    val name: String,
    val coverPhotoId: String? = null,
    val isDefault: Int? = null,
    val photoCount: Int? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

/**
 * 转换为本地 Album 模型
 */
fun AlbumDTO.toAlbum(): Album {
    return Album(
        id = id,
        name = name,
        coverPhotoUrl = null, // 需要从 coverPhotoId 获取
        photoCount = photoCount ?: 0,
        createdAt = createdAt?.let { 
            kotlinx.datetime.Instant.fromEpochMilliseconds(it) 
        } ?: kotlinx.datetime.Clock.System.now(),
        updatedAt = updatedAt?.let { 
            kotlinx.datetime.Instant.fromEpochMilliseconds(it) 
        } ?: kotlinx.datetime.Clock.System.now()
    )
}
