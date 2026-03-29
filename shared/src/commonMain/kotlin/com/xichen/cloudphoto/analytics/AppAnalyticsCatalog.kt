package com.xichen.cloudphoto.analytics

/**
 * Canonical `page` values for [com.xichen.cloudphoto.model.AppEventReportDto.page].
 */
object AnalyticsPages {
    const val PHOTO_TIMELINE = "photo_timeline"
    const val ALBUMS = "albums"
    const val CAMERA = "camera"
    const val STORAGE = "storage"
    const val STORAGE_ADD = "storage_add"
    const val STORAGE_EDIT = "storage_edit"
    const val STORAGE_TUTORIAL = "storage_tutorial"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val ACCOUNT_SECURITY = "account_security"
    const val CHANGE_PASSWORD = "change_password"
    const val THEME_SETTINGS = "theme_settings"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val HELP_FEEDBACK = "help_feedback"
    const val ABOUT = "about"
    const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    const val LOGIN = "login"
    const val REGISTER = "register"
}

/**
 * Event identifiers sent as API `elementId` (文档与报表中的 eventId).
 *
 * Naming: `evt_` + domain + `_` + action[/target].
 */
object AnalyticsEventIds {
    const val BOTTOM_NAV_PHOTOS = "evt_bottom_nav_photos"
    const val BOTTOM_NAV_ALBUMS = "evt_bottom_nav_albums"
    const val BOTTOM_NAV_CAMERA = "evt_bottom_nav_camera"
    const val BOTTOM_NAV_STORAGE = "evt_bottom_nav_storage"
    const val BOTTOM_NAV_SETTINGS = "evt_bottom_nav_settings"

    const val LOGIN_SUBMIT = "evt_login_submit"
    const val LOGIN_GO_REGISTER = "evt_login_go_register"
    const val REGISTER_SUBMIT = "evt_register_submit"
    const val REGISTER_GO_LOGIN = "evt_register_go_login"

    const val PHOTO_SEARCH_TAP = "evt_photo_search_tap"
    const val PHOTO_GRID_ITEM = "evt_photo_grid_item"
    const val PHOTO_FULLSCREEN_CLOSE = "evt_photo_fullscreen_close"

    const val SETTINGS_PROFILE = "evt_settings_profile"
    const val SETTINGS_ACCOUNT_SECURITY = "evt_settings_account_security"
    const val SETTINGS_THEME = "evt_settings_theme"
    const val SETTINGS_NOTIFICATIONS = "evt_settings_notifications"
    const val NOTIFICATION_CLOUD_PUSH_TOGGLE = "evt_notification_cloud_push_toggle"
    const val SETTINGS_HELP = "evt_settings_help"
    const val FEEDBACK_SUBMIT = "evt_feedback_submit"
    const val SETTINGS_ABOUT = "evt_settings_about"
    const val ABOUT_UPLOAD_LOGS = "evt_about_upload_logs"
    const val ABOUT_OPEN_SOURCE_LICENSES = "evt_about_open_source_licenses"
    const val SETTINGS_LOGOUT = "evt_settings_logout_confirm"

    const val STORAGE_ADD_CONFIG = "evt_storage_add_config"
    const val STORAGE_TUTORIAL = "evt_storage_tutorial"
    const val STORAGE_EDIT_CONFIG = "evt_storage_edit_config"
}
