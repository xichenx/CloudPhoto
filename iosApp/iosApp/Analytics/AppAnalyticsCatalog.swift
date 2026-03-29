import Foundation

/// Analytics `page` / `elementId` 字面量，与 KMP `AppAnalyticsCatalog.kt`（`AnalyticsPages`、`AnalyticsEventIds`）及 Android 保持一致。
enum AppAnalyticsCatalog {

    enum Page {
        static let photoTimeline = "photo_timeline"
        static let albums = "albums"
        static let camera = "camera"
        static let storage = "storage"
        static let storageAdd = "storage_add"
        static let storageEdit = "storage_edit"
        static let storageTutorial = "storage_tutorial"
        static let settings = "settings"
        static let profile = "profile"
        static let accountSecurity = "account_security"
        static let changePassword = "change_password"
        static let themeSettings = "theme_settings"
        static let notificationSettings = "notification_settings"
        static let helpFeedback = "help_feedback"
        static let about = "about"
        static let openSourceLicenses = "open_source_licenses"
        static let login = "login"
        static let register = "register"
    }

    enum EventId {
        static let bottomNavPhotos = "evt_bottom_nav_photos"
        static let bottomNavAlbums = "evt_bottom_nav_albums"
        static let bottomNavCamera = "evt_bottom_nav_camera"
        static let bottomNavStorage = "evt_bottom_nav_storage"
        static let bottomNavSettings = "evt_bottom_nav_settings"
        static let loginSubmit = "evt_login_submit"
        static let loginGoRegister = "evt_login_go_register"
        static let registerSubmit = "evt_register_submit"
        static let registerGoLogin = "evt_register_go_login"
        static let photoSearchTap = "evt_photo_search_tap"
        static let photoGridItem = "evt_photo_grid_item"
        static let photoFullscreenClose = "evt_photo_fullscreen_close"
        static let settingsProfile = "evt_settings_profile"
        static let settingsAccountSecurity = "evt_settings_account_security"
        static let settingsTheme = "evt_settings_theme"
        static let settingsNotifications = "evt_settings_notifications"
        static let notificationCloudPushToggle = "evt_notification_cloud_push_toggle"
        static let settingsHelp = "evt_settings_help"
        static let feedbackSubmit = "evt_feedback_submit"
        static let settingsAbout = "evt_settings_about"
        static let aboutUploadLogs = "evt_about_upload_logs"
        static let aboutOpenSourceLicenses = "evt_about_open_source_licenses"
        static let settingsLogout = "evt_settings_logout_confirm"
        static let storageAddConfig = "evt_storage_add_config"
        static let storageTutorial = "evt_storage_tutorial"
        static let storageEditConfig = "evt_storage_edit_config"
    }

    /// 与 `MainTabView` 的 Tab 顺序、Android `Screen.mainTabRoutes` 一致。
    static let mainTabRoutes = ["photos", "albums", "camera", "storage", "settings"]

    /// 将主导航路由（如 `photos`）映射为上报用的 `page` 字段。
    static func page(forMainTabRoute route: String) -> String {
        switch route {
        case "photos": return Page.photoTimeline
        case "albums": return Page.albums
        case "camera": return Page.camera
        case "storage": return Page.storage
        case "settings": return Page.settings
        default:
            let normalized = route.replacingOccurrences(of: "/", with: "_")
            return normalized.isEmpty ? Page.photoTimeline : normalized
        }
    }
}
