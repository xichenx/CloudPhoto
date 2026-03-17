package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 对象存储配置 DTO（与后端 CloudConfigDTO 对应）
 */
@Serializable
data class CloudConfigDTO(
    val id: String? = null,
    val provider: String,
    val endpoint: String? = null,
    val bucket: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null, // 响应时不返回，仅请求时传入
    val region: String? = null,
    val customDomain: String? = null,
    val isActive: Int? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

/**
 * 转换为本地 StorageConfig 模型
 */
fun CloudConfigDTO.toStorageConfig(): StorageConfig {
    return StorageConfig(
        id = id ?: "",
        name = "${provider}-${bucket ?: "unknown"}",
        provider = when (provider.uppercase()) {
            "OSS", "ALIYUN_OSS" -> StorageProvider.ALIYUN_OSS
            "S3", "AWS_S3" -> StorageProvider.AWS_S3
            "COS", "TENCENT_COS" -> StorageProvider.TENCENT_COS
            "MINIO" -> StorageProvider.MINIO
            "QINIU" -> StorageProvider.QINIU
            "CUSTOM_S3" -> StorageProvider.CUSTOM_S3
            else -> StorageProvider.CUSTOM_S3
        },
        endpoint = endpoint ?: "",
        accessKeyId = accessKey ?: "",
        accessKeySecret = secretKey ?: "",
        bucketName = bucket ?: "",
        region = region,
        isDefault = isActive == 1,
        createdAt = createdAt ?: kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    )
}

/**
 * 从本地 StorageConfig 转换为 DTO
 */
fun StorageConfig.toCloudConfigDTO(): CloudConfigDTO {
    return CloudConfigDTO(
        id = id,
        provider = when (provider) {
            StorageProvider.ALIYUN_OSS -> "OSS"
            StorageProvider.AWS_S3 -> "S3"
            StorageProvider.TENCENT_COS -> "COS"
            StorageProvider.MINIO -> "MINIO"
            StorageProvider.QINIU -> "QINIU"
            StorageProvider.CUSTOM_S3 -> "CUSTOM_S3"
        },
        endpoint = endpoint,
        bucket = bucketName,
        accessKey = accessKeyId,
        secretKey = accessKeySecret,
        region = region,
        customDomain = null,
        isActive = if (isDefault) 1 else 0,
        createdAt = createdAt,
        updatedAt = createdAt
    )
}
