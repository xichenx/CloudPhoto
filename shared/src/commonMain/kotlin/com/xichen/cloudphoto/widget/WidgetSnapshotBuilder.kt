package com.xichen.cloudphoto.widget

import com.xichen.cloudphoto.model.Photo
import kotlinx.datetime.Clock

/**
 * Maps domain [Photo] list into a widget-safe payload (URLs only, capped count).
 * Increase [maxPreviewItems] or add fields on [WidgetSnapshotPayload] when extending widgets.
 */
object WidgetSnapshotBuilder {
    private const val DEFAULT_MAX_ITEMS = 4

    fun build(photos: List<Photo>, isLoggedIn: Boolean, maxPreviewItems: Int = DEFAULT_MAX_ITEMS): WidgetSnapshotPayload {
        if (!isLoggedIn) {
            return WidgetSnapshotPayload(
                schemaVersion = WidgetContract.SCHEMA_VERSION,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                photoCount = 0,
                items = emptyList(),
                isLoggedIn = false,
            )
        }
        val sorted = photos.sortedByDescending { it.createdAt }
        val items = sorted
            .take(maxPreviewItems.coerceAtLeast(0))
            .map { photo ->
                WidgetSnapshotItemPayload(
                    id = photo.id,
                    imageUrl = photo.thumbnailUrl?.takeIf { it.isNotBlank() } ?: photo.url,
                    name = photo.name,
                )
            }
        return WidgetSnapshotPayload(
            schemaVersion = WidgetContract.SCHEMA_VERSION,
            updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            photoCount = photos.size,
            items = items,
            isLoggedIn = true,
        )
    }
}
