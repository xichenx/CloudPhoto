package com.xichen.cloudphoto.widget

import kotlinx.serialization.Serializable

/**
 * JSON snapshot consumed by iOS Widget Extension and Android Glance widgets.
 * Field names are stable — keep in sync with Swift `WidgetSnapshotPayload` and docs.
 */
@Serializable
data class WidgetSnapshotPayload(
    val schemaVersion: Int = WidgetContract.SCHEMA_VERSION,
    val updatedAtEpochMillis: Long = 0L,
    val photoCount: Int = 0,
    val items: List<WidgetSnapshotItemPayload> = emptyList(),
    /** When false, widgets show a sign-in prompt instead of empty album. */
    val isLoggedIn: Boolean = true,
)

@Serializable
data class WidgetSnapshotItemPayload(
    val id: String,
    val imageUrl: String,
    val name: String = "",
)
