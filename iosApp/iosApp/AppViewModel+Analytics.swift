import Shared

extension AppViewModel {

    /// 与 Android `AppViewModel.notifyAuthSessionStarted` 一致：登录后上报排队事件并刷新会话 ID。
    func notifyAuthSessionStarted() {
        analyticsTracker.flushPending()
        AnalyticsSession.shared.refresh()
    }

    // MARK: - 通用

    func trackAnalyticsPageView(page: String, fromPage: String?) {
        analyticsTracker.pageView(page: page, fromPage: fromPage)
    }

    /// `targetRoute` / `fromRoute` 使用 Android `Screen` 路由字面量：`photos`、`albums` 等。
    func trackBottomNavClick(targetRoute: String, fromRoute: String) {
        let eventId: String? = {
            switch targetRoute {
            case "photos": return AppAnalyticsCatalog.EventId.bottomNavPhotos
            case "albums": return AppAnalyticsCatalog.EventId.bottomNavAlbums
            case "camera": return AppAnalyticsCatalog.EventId.bottomNavCamera
            case "storage": return AppAnalyticsCatalog.EventId.bottomNavStorage
            case "settings": return AppAnalyticsCatalog.EventId.bottomNavSettings
            default: return nil
            }
        }()
        guard let eventId else { return }
        analyticsTracker.click(
            page: AppAnalyticsCatalog.page(forMainTabRoute: targetRoute),
            elementId: eventId,
            elementType: "tab",
            elementName: nil,
            fromPage: AppAnalyticsCatalog.page(forMainTabRoute: fromRoute),
            position: nil,
            extra: nil
        )
    }

    func trackClick(
        page: String,
        eventId: String,
        elementType: String,
        elementName: String? = nil,
        fromPage: String? = nil,
        position: KotlinInt? = nil,
        extra: String? = nil
    ) {
        analyticsTracker.click(
            page: page,
            elementId: eventId,
            elementType: elementType,
            elementName: elementName,
            fromPage: fromPage,
            position: position,
            extra: extra
        )
    }

    // MARK: - 认证

    func trackLoginSubmit() {
        trackClick(page: AppAnalyticsCatalog.Page.login, eventId: AppAnalyticsCatalog.EventId.loginSubmit, elementType: "button", elementName: "登录")
    }

    func trackLoginGoRegister() {
        trackClick(page: AppAnalyticsCatalog.Page.login, eventId: AppAnalyticsCatalog.EventId.loginGoRegister, elementType: "button", elementName: "立即注册")
    }

    func trackRegisterSubmit() {
        trackClick(page: AppAnalyticsCatalog.Page.register, eventId: AppAnalyticsCatalog.EventId.registerSubmit, elementType: "button", elementName: "注册")
    }

    func trackRegisterGoLogin() {
        trackClick(page: AppAnalyticsCatalog.Page.register, eventId: AppAnalyticsCatalog.EventId.registerGoLogin, elementType: "button", elementName: "去登录")
    }

    func trackPageViewLogin(fromPage: String?) {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.login, fromPage: fromPage)
    }

    func trackPageViewRegister(fromPage: String?) {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.register, fromPage: fromPage)
    }

    // MARK: - 照片

    func trackPhotoSearchTap() {
        trackClick(
            page: AppAnalyticsCatalog.Page.photoTimeline,
            eventId: AppAnalyticsCatalog.EventId.photoSearchTap,
            elementType: "button",
            elementName: "搜索"
        )
    }

    func trackPhotoGridItem(photo: Photo, positionOneBased: Int) {
        let extra = "{\"photoId\":\"\(photo.id)\"}"
        trackClick(
            page: AppAnalyticsCatalog.Page.photoTimeline,
            eventId: AppAnalyticsCatalog.EventId.photoGridItem,
            elementType: "image",
            elementName: photo.name,
            fromPage: nil,
            position: KotlinInt(value: Int32(positionOneBased)),
            extra: extra
        )
    }

    func trackPhotoFullscreenClose() {
        trackClick(
            page: AppAnalyticsCatalog.Page.photoTimeline,
            eventId: AppAnalyticsCatalog.EventId.photoFullscreenClose,
            elementType: "button",
            elementName: "关闭全屏"
        )
    }

    // MARK: - 设置

    func trackSettingsProfileTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsProfile, elementType: "list_item", elementName: "个人资料")
    }

    func trackSettingsAccountSecurityTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsAccountSecurity, elementType: "list_item", elementName: "账户安全")
    }

    func trackSettingsThemeTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsTheme, elementType: "list_item", elementName: "主题设置")
    }

    func trackSettingsNotificationTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsNotifications, elementType: "list_item", elementName: "消息通知")
    }

    func trackPageViewNotificationSettings() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.notificationSettings, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    func trackNotificationCloudPushToggle(enabled: Bool) {
        trackClick(
            page: AppAnalyticsCatalog.Page.notificationSettings,
            eventId: AppAnalyticsCatalog.EventId.notificationCloudPushToggle,
            elementType: "switch",
            elementName: enabled ? "开启云端推送" : "关闭云端推送"
        )
    }

    func trackSettingsHelpTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsHelp, elementType: "list_item", elementName: "帮助与反馈")
    }

    func trackSettingsAboutTap() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsAbout, elementType: "list_item", elementName: "关于")
    }

    func trackSettingsLogoutConfirm() {
        trackClick(page: AppAnalyticsCatalog.Page.settings, eventId: AppAnalyticsCatalog.EventId.settingsLogout, elementType: "button", elementName: "确认退出")
    }

    func trackPageViewProfile() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.profile, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    func trackPageViewAccountSecurity() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.accountSecurity, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    func trackPageViewChangePassword() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.changePassword, fromPage: AppAnalyticsCatalog.Page.accountSecurity)
    }

    func trackPageViewThemeSettings() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.themeSettings, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    func trackPageViewHelpFeedback() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.helpFeedback, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    func trackPageViewAbout() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.about, fromPage: AppAnalyticsCatalog.Page.settings)
    }

    // MARK: - 存储

    func trackStorageAddConfig(elementName: String) {
        trackClick(page: AppAnalyticsCatalog.Page.storage, eventId: AppAnalyticsCatalog.EventId.storageAddConfig, elementType: "button", elementName: elementName)
    }

    func trackStorageTutorial() {
        trackClick(page: AppAnalyticsCatalog.Page.storage, eventId: AppAnalyticsCatalog.EventId.storageTutorial, elementType: "list_item", elementName: "配置教程")
    }

    func trackStorageEditConfig(configId: String) {
        let extra = "{\"configId\":\"\(configId)\"}"
        trackClick(
            page: AppAnalyticsCatalog.Page.storage,
            eventId: AppAnalyticsCatalog.EventId.storageEditConfig,
            elementType: "button",
            elementName: "编辑配置",
            fromPage: nil,
            position: nil,
            extra: extra
        )
    }

    func trackPageViewStorageAdd() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.storageAdd, fromPage: AppAnalyticsCatalog.Page.storage)
    }

    func trackPageViewStorageEdit() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.storageEdit, fromPage: AppAnalyticsCatalog.Page.storage)
    }

    func trackPageViewStorageTutorial() {
        trackAnalyticsPageView(page: AppAnalyticsCatalog.Page.storageTutorial, fromPage: AppAnalyticsCatalog.Page.storage)
    }
}
