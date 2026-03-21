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
    
    private let photoService: PhotoService
    private let configService: ConfigService
    private let albumService: AlbumService
    private let authService: AuthService
    private let tokenManager: TokenManager
    
    init() {
        DiagnosticLogging.shared.install(rootContext: nil)
        let container = AppContainerHolder.shared.getContainer(context: nil)
        container.startDiagnosticLogUpload()
        self.photoService = container.photoService
        self.configService = container.configService
        self.albumService = container.albumService
        self.authService = container.authService
        self.tokenManager = TokenManager(context: nil)
        
        checkLoginStatus()
        if isLoggedIn {
            loadPhotos()
            loadConfigs()
        }
    }
    
    private func checkLoginStatus() {
        isLoggedIn = tokenManager.isLoggedIn()
    }
    
    func loadPhotos() {
        Task {
            do {
                photos = try await photoService.getAllPhotos()
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
                let kotlinByteArray = KotlinByteArray(size: Int32(photoData.count))
                for (index, byte) in photoData.enumerated() {
                    kotlinByteArray.set(index: Int32(index), value: Int8(bitPattern: byte))
                }
                
                _ = try await photoService.uploadPhotoThrowing(
                    photoData: kotlinByteArray,
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
                    loadPhotos()
                    loadConfigs()
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
                    // 注册成功后自动登录
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
    
    /// Mock 登录（开发测试用）
    func mockLogin() {
        authError = nil
        let mockUser = UserDTO(
            id: "mock_user_001",
            username: "测试用户",
            email: "test@example.com",
            phone: "13800138000",
            role: "user",
            avatar: nil,
            createdAt: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000))
        )
        tokenManager.saveAccessToken(token: "mock_access_token_\(Int64(Date().timeIntervalSince1970))")
        tokenManager.saveRefreshToken(token: "mock_refresh_token_\(Int64(Date().timeIntervalSince1970))")
        currentUser = mockUser
        isLoggedIn = true
        loadPhotos()
        loadConfigs()
    }
    
    /// 登出
    func logout() {
        if let accessToken = tokenManager.getAccessToken(), let refreshToken = tokenManager.getRefreshToken() {
            Task {
                _ = try? await authService.logout(accessToken: accessToken, refreshToken: refreshToken)
            }
        }
        tokenManager.clearTokens()
        currentUser = nil
        isLoggedIn = false
    }
    
    /// 清除认证错误（UI 展示 Toast 后调用）
    func clearAuthError() {
        authError = nil
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

