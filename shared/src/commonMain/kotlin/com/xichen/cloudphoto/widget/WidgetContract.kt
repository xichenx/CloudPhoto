package com.xichen.cloudphoto.widget

/**
 * Cross-platform constants for home screen widgets, deep links, and snapshot files.
 * Extend this object when adding new widget kinds or link targets.
 */
object WidgetContract {
    const val SCHEMA_VERSION: Int = 1

    /** Single JSON file name (same on both platforms). */
    const val SNAPSHOT_FILE_NAME: String = "widget_snapshot.json"

    /** iOS App Group identifier — must match entitlements on app + widget extension. */
    const val IOS_APP_GROUP_ID: String = "group.com.xichen.cloudphoto"

    /** WidgetKit `kind` string — must match Swift `WidgetConfiguration(kind:)`. */
    const val WIDGET_KIND_RECENT_PHOTOS: String = "RecentPhotosWidget"

    const val DEEP_LINK_SCHEME: String = "cloudphoto"
    const val DEEP_LINK_HOST_PHOTOS: String = "photos"
    const val DEEP_LINK_HOST_CAMERA: String = "camera"

    val DEEP_LINK_PHOTOS: String = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST_PHOTOS"
    val DEEP_LINK_CAMERA: String = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST_CAMERA"
}
