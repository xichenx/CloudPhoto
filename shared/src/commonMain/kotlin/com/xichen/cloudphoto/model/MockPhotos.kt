package com.xichen.cloudphoto.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * 本地无照片时用于预览网格的 mock 数据（Android / iOS 共用）。
 */
object MockPhotos {

    const val FUJI_SAMPLE_URL: String =
        "https://img.freepik.com/free-photo/beautiful-fuji-mountain-yamanakako-yamanaka-lake_74190-3026.jpg?t=st=1773973998~exp=1773977598~hmac=42024c006ad7a63ccbf6d0d736f1c6e01976fbc487ecc91450969139c7153936&w=2000"

    fun tenFujiGrid(): List<Photo> {
        val now = Clock.System.now()
        return (1..30).map { index ->
            val daysAgo = (10 - index).toLong()
            val createdAt = Instant.fromEpochMilliseconds(
                now.toEpochMilliseconds() - (daysAgo * 24 * 60 * 60 * 1000)
            )
            Photo(
                id = "mock_photo_$index",
                name = "Mock Photo $index",
                url = FUJI_SAMPLE_URL,
                thumbnailUrl = FUJI_SAMPLE_URL,
                size = 1024 * 1024L,
                width = 2000,
                height = 1333,
                mimeType = "image/jpeg",
                createdAt = createdAt,
                albumId = null,
                storageConfigId = "mock_config"
            )
        }
    }
}
