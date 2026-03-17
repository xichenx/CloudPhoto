package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

@Serializable
data class StorageConfig(
    val id: String,
    val name: String,
    val provider: StorageProvider,
    val endpoint: String,
    val accessKeyId: String,
    val accessKeySecret: String,
    val bucketName: String,
    val region: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long
)

@Serializable
enum class StorageProvider {
    ALIYUN_OSS,
    AWS_S3,
    TENCENT_COS,
    MINIO,
    QINIU,
    CUSTOM_S3
}

