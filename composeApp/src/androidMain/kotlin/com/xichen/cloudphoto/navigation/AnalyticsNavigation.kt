package com.xichen.cloudphoto.navigation

import com.xichen.cloudphoto.analytics.AnalyticsPages

/**
 * Maps NavController route to backend `page` field for `/api/events`.
 */
fun String.toAnalyticsPage(): String = when (this) {
    Screen.Photos.route -> AnalyticsPages.PHOTO_TIMELINE
    Screen.Albums.route -> AnalyticsPages.ALBUMS
    Screen.Camera.route -> AnalyticsPages.CAMERA
    Screen.Storage.route -> AnalyticsPages.STORAGE
    Screen.Settings.route -> AnalyticsPages.SETTINGS
    Screen.AddStorageConfig.route -> AnalyticsPages.STORAGE_ADD
    Screen.StorageTutorial.route -> AnalyticsPages.STORAGE_TUTORIAL
    Screen.Profile.route -> AnalyticsPages.PROFILE
    Screen.AccountSecurity.route -> AnalyticsPages.ACCOUNT_SECURITY
    Screen.ChangePassword.route -> AnalyticsPages.CHANGE_PASSWORD
    Screen.ThemeSettings.route -> AnalyticsPages.THEME_SETTINGS
    else -> if (startsWith("storage/edit/")) {
        AnalyticsPages.STORAGE_EDIT
    } else {
        replace('/', '_').ifBlank { AnalyticsPages.PHOTO_TIMELINE }
    }
}
