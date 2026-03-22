package com.xichen.cloudphoto.analytics

/**
 * Device and app fields required by the backend `/api/events` contract (`platform`, `appVersion`, etc.).
 */
data class AnalyticsDeviceMeta(
    val platform: String,
    val appVersion: String,
    val osVersion: String,
    val deviceModel: String?
)

/**
 * Resolved on each platform; [context] is the Android [android.content.Context] when available.
 */
expect fun analyticsDeviceMeta(context: Any? = null): AnalyticsDeviceMeta
