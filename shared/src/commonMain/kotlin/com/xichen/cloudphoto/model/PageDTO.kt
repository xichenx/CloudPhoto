package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 分页响应 DTO（与后端 PageDTO 对应）
 */
@Serializable
data class PageDTO<T>(
    val records: List<T>,
    val total: Long? = null,
    val pages: Long? = null,
    val current: Long? = null,
    val size: Long? = null
)
