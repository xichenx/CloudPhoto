import Foundation
import Shared
import Combine

@MainActor
class AppViewModel: ObservableObject {
    @Published var photos: [Photo] = []
    @Published var configs: [StorageConfig] = []
    @Published var defaultConfig: StorageConfig? = nil

    /// 登录状态，未登录时显示登录/注册界面
    @Published var isLoggedIn: Bool = false
    /// 当前用户信息（登录后有效）
    @Published var currentUser: UserDTO? = nil
    /// 认证相关错误信息（登录/注册/发送验证码失败时设置，UI 展示后建议清空）
    @Published var authError: String? = nil
    /// 修改密码成功（单次，UI 消费后调用 clearChangePasswordSuccess）
    @Published var changePasswordSuccess: Bool = false

    @Published var feedbackSubmitting: Bool = false
    @Published var feedbackSuccess: Bool = false
    @Published var feedbackError: String? = nil

    @Published var diagnosticLogUploading: Bool = false
    @Published var diagnosticLogToast: String?
    @Published var diagnosticLogToastType: ToastType = .info

    /// 云端是否下发推送；`nil` 表示未加载成功
    @Published var cloudPushEnabled: Bool? = nil
    @Published var cloudPushPreferenceLoading: Bool = false
    @Published var cloudPushPreferenceSaving: Bool = false
    @Published var pushPreferenceToast: String?
    @Published var pushPreferenceToastType: ToastType = .info

    private let photoService: PhotoService
    private let configService: ConfigService
    private let authService: AuthService
    private let feedbackApiService: FeedbackApiService
    private let userPushPreferenceApiService: UserPushPreferenceApiService
    private let tokenManager: TokenManager
    /// `internal`：供 `AppViewModel+Analytics` 扩展使用。
    let analyticsTracker: AnalyticsTracker

    init() {
        DiagnosticLogging.shared.install(rootContext: nil)
        let container = AppContainerHolder.shared.getContainer(context: nil)
        container.startDiagnosticLogUpload()
        self.photoService = container.photoService
        self.configService = container.configService
        self.authService = container.authService
        self.feedbackApiService = container.feedbackApiService
        self.userPushPreferenceApiService = container.userPushPreferenceApiService
        self.tokenManager = TokenManager(context: nil)
        self.analyticsTracker = container.analyticsTracker

        checkLoginStatus()
        if isLoggedIn {
            loadPhotos()
            loadConfigs()
            IosPushRegistration.shared.syncApnsTokenAfterLogin(tokenHex: AppDelegate.pendingApnsTokenHex)
        } else {
            WidgetSnapshotSync.shared.publishFromPhotos(photos: [], isLoggedIn: false, platformContext: nil)
            WidgetTimelineReloader.reloadRecentPhotos()
        }
    }

    private func checkLoginStatus() {
        isLoggedIn = tokenManager.isLoggedIn()
    }

    // MARK: - 照片与配置

    func loadPhotos() {
        Task {
            do {
                if isLoggedIn {
                    photos = try await photoService.fetchTimelineFromCloud()
                } else {
                    photos = []
                }
                WidgetSnapshotSync.shared.publishFromPhotos(photos: photos, isLoggedIn: isLoggedIn, platformContext: nil)
                WidgetTimelineReloader.reloadRecentPhotos()
            } catch {
                print("Error loading photos: \(error)")
            }
        }
    }

    func loadConfigs() {
        Task {
            do {
                configs = try await configService.getAllConfigs()
                defaultConfig = try await configService.getDefaultConfig()
            } catch {
                print("Error loading configs: \(error)")
            }
        }
    }

    func uploadPhoto(photoData: Data, fileName: String, mimeType: String, width: Int32, height: Int32) {
        Task {
            do {
                _ = try await photoService.uploadPhotoThrowing(
                    photoData: photoData.toKotlinByteArray(),
                    fileName: fileName,
                    mimeType: mimeType,
                    width: width,
                    height: height,
                    configId: nil,
                    albumId: nil
                )
                loadPhotos()
            } catch {
                print("Error uploading photo: \(error)")
            }
        }
    }

    func deletePhoto(photoId: String) {
        Task {
            do {
                try await photoService.deletePhotoThrowing(photoId: photoId)
                loadPhotos()
            } catch {
                print("Error deleting photo: \(error)")
            }
        }
    }

    func saveConfig(config: StorageConfig) {
        Task {
            do {
                try await configService.saveConfig(config: config)
                loadConfigs()
            } catch {
                print("Error saving config: \(error)")
            }
        }
    }

    func deleteConfig(configId: String) {
        Task {
            do {
                try await configService.deleteConfig(id: configId)
                loadConfigs()
            } catch {
                print("Error deleting config: \(error)")
            }
        }
    }

    func setDefaultConfig(configId: String) {
        Task {
            do {
                try await configService.setDefaultConfig(id: configId)
                loadConfigs()
            } catch {
                print("Error setting default config: \(error)")
            }
        }
    }

    // MARK: - 认证

    /// 登录（邮箱 + 密码）
    func login(account: String, password: String) {
        authError = nil
        Task {
            do {
                let request = LoginRequest(account: account, password: password)
                let outcome = try await authService.loginOutcome(request: request)
                await MainActor.run {
                    if let response = outcome.first {
                        tokenManager.saveAccessToken(token: response.accessToken)
                        tokenManager.saveRefreshToken(token: response.refreshToken)
                        currentUser = response.user
                        isLoggedIn = true
                        notifyAuthSessionStarted()
                        loadPhotos()
                        loadConfigs()
                        IosPushRegistration.shared.syncApnsTokenAfterLogin(tokenHex: AppDelegate.pendingApnsTokenHex)
                    } else if let message = outcome.second {
                        authError = String(message)
                    }
                }
            } catch {
                await MainActor.run { authError = error.localizedDescription }
            }
        }
    }

    /// 注册（邮箱 + 邮箱验证码）
    func register(username: String, email: String, password: String, emailCode: String) {
        authError = nil
        Task {
            do {
                let request = RegisterRequest(
                    username: username,
                    password: password,
                    email: email,
                    emailCode: emailCode
                )
                let outcome = try await authService.registerOutcome(request: request)
                await MainActor.run {
                    if outcome.first != nil {
                        login(account: email, password: password)
                    } else if let message = outcome.second {
                        authError = String(message)
                    }
                }
            } catch {
                await MainActor.run { authError = error.localizedDescription }
            }
        }
    }

    /// 发送邮箱验证码
    func sendEmailCode(email: String, type: String = "register") {
        authError = nil
        Task {
            do {
                let outcome = try await authService.sendEmailCodeOutcome(email: email, type: type)
                await MainActor.run {
                    let success = outcome.first?.boolValue ?? true
                    if !success, let message = outcome.second {
                        authError = String(message)
                    }
                }
            } catch {
                await MainActor.run { authError = error.localizedDescription }
            }
        }
    }

    /// 登出
    func logout() {
        IosPushRegistration.shared.unregisterOnLogout {
            DispatchQueue.main.async {
                self.finishLogoutAfterPushUnregister()
            }
        }
    }

    private func finishLogoutAfterPushUnregister() {
        if let accessToken = tokenManager.getAccessToken(), let refreshToken = tokenManager.getRefreshToken() {
            Task {
                _ = try? await authService.logout(accessToken: accessToken, refreshToken: refreshToken)
            }
        }
        tokenManager.clearTokens()
        analyticsTracker.clearPending()
        AnalyticsSession.shared.refresh()
        currentUser = nil
        isLoggedIn = false
        WidgetSnapshotSync.shared.publishFromPhotos(photos: [], isLoggedIn: false, platformContext: nil)
        WidgetTimelineReloader.reloadRecentPhotos()
    }

    /// 清除认证错误（UI 展示 Toast 后调用）
    func clearAuthError() {
        authError = nil
    }

    func clearFeedbackUiState() {
        feedbackSuccess = false
        feedbackError = nil
    }

    func clearDiagnosticLogToast() {
        diagnosticLogToast = nil
    }

    func clearPushPreferenceToast() {
        pushPreferenceToast = nil
    }

    /// 从服务端加载云端推送开关（需登录）。
    func loadCloudPushPreference() {
        guard isLoggedIn else {
            cloudPushEnabled = nil
            pushPreferenceToast = "请先登录"
            pushPreferenceToastType = .warning
            return
        }
        cloudPushPreferenceLoading = true
        Task {
            do {
                let outcome = try await userPushPreferenceApiService.loadOutcome()
                await MainActor.run {
                    cloudPushPreferenceLoading = false
                    if let v = outcome.first {
                        cloudPushEnabled = v.boolValue
                    } else {
                        cloudPushEnabled = nil
                        pushPreferenceToast = outcome.second.map { String(describing: $0) } ?? "加载失败"
                        pushPreferenceToastType = .error
                    }
                }
            } catch {
                await MainActor.run {
                    cloudPushPreferenceLoading = false
                    cloudPushEnabled = nil
                    pushPreferenceToast = error.localizedDescription
                    pushPreferenceToastType = .error
                }
            }
        }
    }

    /// 更新云端推送开关（需登录）。
    func setCloudPushEnabled(_ enabled: Bool) {
        guard isLoggedIn else {
            pushPreferenceToast = "请先登录"
            pushPreferenceToastType = .warning
            return
        }
        let previous = cloudPushEnabled
        cloudPushPreferenceSaving = true
        trackNotificationCloudPushToggle(enabled: enabled)
        Task {
            do {
                let outcome = try await userPushPreferenceApiService.updateOutcome(pushEnabled: enabled)
                await MainActor.run {
                    cloudPushPreferenceSaving = false
                    if outcome.first?.boolValue == true {
                        cloudPushEnabled = enabled
                        pushPreferenceToast = "已保存"
                        pushPreferenceToastType = .success
                    } else {
                        cloudPushEnabled = previous
                        pushPreferenceToast = outcome.second.map { String(describing: $0) } ?? "保存失败"
                        pushPreferenceToastType = .error
                    }
                }
            } catch {
                await MainActor.run {
                    cloudPushPreferenceSaving = false
                    cloudPushEnabled = previous
                    pushPreferenceToast = error.localizedDescription
                    pushPreferenceToastType = .error
                }
            }
        }
    }

    /// 主动上传本机诊断日志队列（需登录；与后台定时上传开关独立）。
    func uploadDiagnosticLogsNow() {
        guard isLoggedIn else {
            diagnosticLogToast = "请先登录"
            diagnosticLogToastType = .warning
            return
        }
        diagnosticLogUploading = true
        Task {
            do {
                let outcome = try await RemoteLogUploadScheduler.shared.uploadDiagnosticLogsNow()
                await MainActor.run {
                    diagnosticLogUploading = false
                    // 与 KMP [UploadDiagnosticLogsOutcome] CODE_* 常量一致
                    let code = Int(outcome.code)
                    if code == 0 {
                        diagnosticLogToast = "诊断日志已上传"
                        diagnosticLogToastType = .success
                    } else if code == 1 {
                        diagnosticLogToast = "暂无待上传日志"
                        diagnosticLogToastType = .info
                    } else {
                        diagnosticLogToast = outcome.message ?? "上传失败"
                        diagnosticLogToastType = .error
                    }
                }
            } catch {
                await MainActor.run {
                    diagnosticLogUploading = false
                    diagnosticLogToast = error.localizedDescription
                    diagnosticLogToastType = .error
                }
            }
        }
    }

    /// 提交用户反馈（需已登录；分类：`bug` / `suggestion` / `general`）。身份由服务端根据 Token 解析。
    func submitFeedback(content: String, category: String) {
        feedbackSubmitting = true
        feedbackError = nil
        feedbackSuccess = false
        Task {
            do {
                let outcome = try await feedbackApiService.submitOutcome(content: content, category: category)
                await MainActor.run {
                    feedbackSubmitting = false
                    if outcome.first?.boolValue == true {
                        feedbackSuccess = true
                    } else if let msg = outcome.second {
                        feedbackError = String(describing: msg)
                    }
                }
            } catch {
                await MainActor.run {
                    feedbackSubmitting = false
                    feedbackError = error.localizedDescription
                }
            }
        }
    }

    /// 修改密码成功标志清零（UI 消费后调用）
    func clearChangePasswordSuccess() {
        changePasswordSuccess = false
    }

    /// 修改密码（需先通过邮箱验证码验证）
    func changePassword(email: String, emailCode: String, oldPassword: String, newPassword: String) {
        authError = nil
        Task {
            do {
                let request = ChangePasswordRequest(
                    email: email,
                    emailCode: emailCode,
                    oldPassword: oldPassword,
                    newPassword: newPassword
                )
                let outcome = try await authService.changePasswordOutcome(request: request)
                await MainActor.run {
                    let success = outcome.first?.boolValue ?? false
                    if success {
                        changePasswordSuccess = true
                    } else if let message = outcome.second {
                        authError = String(message)
                    }
                }
            } catch {
                await MainActor.run { authError = error.localizedDescription }
            }
        }
    }

    /// 更新个人资料（用户名、头像等）
    func updateProfile(username: String?, avatar: String? = nil) {
        authError = nil
        guard let token = tokenManager.getAccessToken() else {
            authError = "请先登录"
            return
        }
        Task {
            do {
                let request = UpdateProfileRequest(username: username, avatar: avatar)
                let outcome = try await authService.updateProfileOutcome(accessToken: token, request: request)
                await MainActor.run {
                    if let user = outcome.first {
                        currentUser = user
                    } else if let message = outcome.second {
                        authError = String(message)
                    }
                }
            } catch {
                await MainActor.run { authError = error.localizedDescription }
            }
        }
    }
}
