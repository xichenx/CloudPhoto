package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * Client payload for POST `/api/events` and batch `events[]` (matches backend [AppEventReportDTO] JSON).
 */
@Serializable
data class AppEventReportDto(
    val eventType: String,
    val sessionId: String? = null,
    val page: String? = null,
    val fromPage: String? = null,
    val elementId: String? = null,
    val elementType: String? = null,
    val elementName: String? = null,
    val position: Int? = null,
    val exposureDurationMs: Long? = null,
    val platform: String? = null,
    val appVersion: String? = null,
    val deviceModel: String? = null,
    val osVersion: String? = null,
    val extra: String? = null,
    val clientTimestamp: Long? = null
)

@Serializable
data class AppEventBatchReportDto(
    val events: List<AppEventReportDto>
)
