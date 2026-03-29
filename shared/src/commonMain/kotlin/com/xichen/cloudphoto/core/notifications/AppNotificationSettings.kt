package com.xichen.cloudphoto.core.notifications

/**
 * Opens the OS screen where the user can enable or manage notifications for this app.
 *
 * @param appContext Android: [android.content.Context]; iOS: unused (pass `null`).
 */
expect object AppNotificationSettings {
    fun open(appContext: Any?)
}
