package com.xichen.cloudphoto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xichen.cloudphoto.model.Photo
import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.analytics.AnalyticsEventIds
import com.xichen.cloudphoto.analytics.AnalyticsPages
import com.xichen.cloudphoto.analytics.AnalyticsSession
import com.xichen.cloudphoto.core.di.AppContainerHolder
import com.xichen.cloudphoto.core.error.ErrorHandler
import com.xichen.cloudphoto.core.theme.ThemeMode
import com.xichen.cloudphoto.core.theme.ThemeRepository
import com.xichen.cloudphoto.core.ToastType
import com.xichen.cloudphoto.core.logger.DiagnosticLogging
import com.xichen.cloudphoto.core.logger.Log
import com.xichen.cloudphoto.core.logger.RemoteLogUploadScheduler
import com.xichen.cloudphoto.core.logger.UploadDiagnosticLogsOutcome
import com.xichen.cloudphoto.service.AlbumService
import com.xichen.cloudphoto.service.AuthService
import com.xichen.cloudphoto.service.ConfigApiService
import com.xichen.cloudphoto.service.ConfigService
import com.xichen.cloudphoto.service.FeedbackApiService
import com.xichen.cloudphoto.service.PhotoService
import com.xichen.cloudphoto.service.UserPushPreferenceApiService
import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.auth.AuthEvent
import com.xichen.cloudphoto.core.auth.AuthEvents
import com.xichen.cloudphoto.core.network.*
import com.xichen.cloudphoto.model.*
import com.xichen.cloudphoto.navigation.Screen
import com.xichen.cloudphoto.push.PushRegistrationAndroid
import com.xichen.cloudphoto.widget.HomeWidgetUpdater
import com.xichen.cloudphoto.widget.WidgetContract
import com.xichen.cloudphoto.widget.WidgetSnapshotSync
import com.xichen.cloudphoto.push.pushInstallId
import com.xichen.cloudphoto.navigation.toAnalyticsPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogUploadUiEvent(
    val message: String,
    val type: ToastType,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    // 使用依赖注入容器（传递 Application Context）
    private val container = AppContainerHolder.getContainer(application.applicationContext)
    
    // 初始化 Repositories
    private val themeRepository: ThemeRepository = container.themeRepository

    init {
        DiagnosticLogging.install(application.applicationContext)
        container.configRepository.init(application)
        container.photoRepository.init(application)
        container.albumRepository.init(application)
        themeRepository.init(application)
        container.startDiagnosticLogUpload()
    }
    
    // 从容器获取服务（含认证服务，接口 baseUrl 在 shared ApiConfig 中统一配置）
    private val configService: ConfigService = container.configService  // 本地服务（作为缓存）
    private val configApiService: ConfigApiService = container.configApiService  // 后端API服务
    private val photoService: PhotoService = container.photoService
    private val albumService: AlbumService = container.albumService
    private val authService: AuthService = container.authService
    private val feedbackApiService: FeedbackApiService = container.feedbackApiService
    private val userPushPreferenceApiService: UserPushPreferenceApiService =
        container.userPushPreferenceApiService

    // Token管理器 - 使用容器中的 TokenManager（统一管理）
    private val tokenManager: TokenManager = container.tokenManager
    
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    
    private val _configs = MutableStateFlow<List<StorageConfig>>(emptyList())
    val configs: StateFlow<List<StorageConfig>> = _configs.asStateFlow()
    
    private val _defaultConfig = MutableStateFlow<StorageConfig?>(null)
    val defaultConfig: StateFlow<StorageConfig?> = _defaultConfig.asStateFlow()
    
    // 登录状态管理
    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    // 当前用户信息
    private val _currentUser = MutableStateFlow<UserDTO?>(null)
    val currentUser: StateFlow<UserDTO?> = _currentUser.asStateFlow()
    
    // 错误消息
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // 修改密码成功（单次消费，UI 消费后调用 clearChangePasswordSuccess）
    private val _changePasswordSuccess = MutableStateFlow(false)
    val changePasswordSuccess: StateFlow<Boolean> = _changePasswordSuccess.asStateFlow()

    /** 主题模式：跟随系统 / 浅色 / 深色 */
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _feedbackSubmitting = MutableStateFlow(false)
    val feedbackSubmitting: StateFlow<Boolean> = _feedbackSubmitting.asStateFlow()

    private val _feedbackSuccess = MutableStateFlow(false)
    val feedbackSuccess: StateFlow<Boolean> = _feedbackSuccess.asStateFlow()

    private val _feedbackError = MutableStateFlow<String?>(null)
    val feedbackError: StateFlow<String?> = _feedbackError.asStateFlow()

    private val _diagnosticLogUploading = MutableStateFlow(false)
    val diagnosticLogUploading: StateFlow<Boolean> = _diagnosticLogUploading.asStateFlow()

    private val _logUploadUi = MutableStateFlow<LogUploadUiEvent?>(null)
    val logUploadUi: StateFlow<LogUploadUiEvent?> = _logUploadUi.asStateFlow()

    /** 云端是否向本账号下发推送（与系统通知权限无关）；`null` 表示尚未加载成功。 */
    private val _cloudPushEnabled = MutableStateFlow<Boolean?>(null)
    val cloudPushEnabled: StateFlow<Boolean?> = _cloudPushEnabled.asStateFlow()

    private val _cloudPushPreferenceLoading = MutableStateFlow(false)
    val cloudPushPreferenceLoading: StateFlow<Boolean> = _cloudPushPreferenceLoading.asStateFlow()

    private val _cloudPushPreferenceSaving = MutableStateFlow(false)
    val cloudPushPreferenceSaving: StateFlow<Boolean> = _cloudPushPreferenceSaving.asStateFlow()

    private val _pushPreferenceUi = MutableStateFlow<LogUploadUiEvent?>(null)
    val pushPreferenceUi: StateFlow<LogUploadUiEvent?> = _pushPreferenceUi.asStateFlow()

    /** Widget / deep link: navigate to main tab when user opens `cloudphoto://…`. */
    private val _pendingMainTabRoute = MutableStateFlow<String?>(null)
    val pendingMainTabRoute: StateFlow<String?> = _pendingMainTabRoute.asStateFlow()

    fun clearPendingMainTabRoute() {
        _pendingMainTabRoute.value = null
    }

    fun handleWidgetDeepLinkFromIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != WidgetContract.DEEP_LINK_SCHEME) return
        when (uri.host) {
            WidgetContract.DEEP_LINK_HOST_PHOTOS -> _pendingMainTabRoute.value = Screen.Photos.route
            WidgetContract.DEEP_LINK_HOST_CAMERA -> _pendingMainTabRoute.value = Screen.Camera.route
        }
    }

    fun clearLogUploadUi() {
        _logUploadUi.value = null
    }

    fun clearPushPreferenceUi() {
        _pushPreferenceUi.value = null
    }

    /**
     * 从服务端拉取云端推送开关（需登录）。
     */
    fun loadCloudPushPreference() {
        viewModelScope.launch {
            if (!tokenManager.isLoggedIn()) {
                _cloudPushEnabled.value = null
                _pushPreferenceUi.value = LogUploadUiEvent("请先登录", ToastType.WARNING)
                return@launch
            }
            _cloudPushPreferenceLoading.value = true
            val (value, err) = userPushPreferenceApiService.loadOutcome()
            _cloudPushPreferenceLoading.value = false
            if (value != null) {
                _cloudPushEnabled.value = value
            } else {
                _cloudPushEnabled.value = null
                _pushPreferenceUi.value = LogUploadUiEvent(
                    err ?: "加载失败",
                    ToastType.ERROR,
                )
            }
        }
    }

    /**
     * 更新服务端云端推送开关（需登录）。
     */
    fun setCloudPushEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!tokenManager.isLoggedIn()) {
                _pushPreferenceUi.value = LogUploadUiEvent("请先登录", ToastType.WARNING)
                return@launch
            }
            val previous = _cloudPushEnabled.value
            _cloudPushPreferenceSaving.value = true
            container.analyticsTracker.click(
                page = AnalyticsPages.NOTIFICATION_SETTINGS,
                elementId = AnalyticsEventIds.NOTIFICATION_CLOUD_PUSH_TOGGLE,
                elementType = "switch",
                elementName = if (enabled) "开启云端推送" else "关闭云端推送",
            )
            val (ok, msg) = userPushPreferenceApiService.updateOutcome(enabled)
            _cloudPushPreferenceSaving.value = false
            if (ok) {
                _cloudPushEnabled.value = enabled
                _pushPreferenceUi.value = LogUploadUiEvent("已保存", ToastType.SUCCESS)
            } else {
                _cloudPushEnabled.value = previous
                _pushPreferenceUi.value = LogUploadUiEvent(
                    msg ?: "保存失败",
                    ToastType.ERROR,
                )
            }
        }
    }

    /**
     * 将本地队列中的诊断日志全部上传（需登录；与 [RemoteLogConfig.periodicRemoteUploadEnabled] 独立）。
     */
    fun uploadDiagnosticLogsNow() {
        viewModelScope.launch {
            if (!tokenManager.isLoggedIn()) {
                _logUploadUi.value = LogUploadUiEvent("请先登录", ToastType.WARNING)
                return@launch
            }
            _diagnosticLogUploading.value = true
            val outcome = RemoteLogUploadScheduler.uploadDiagnosticLogsNow()
            _diagnosticLogUploading.value = false
            _logUploadUi.value = when (outcome.code) {
                UploadDiagnosticLogsOutcome.CODE_UPLOADED ->
                    LogUploadUiEvent("诊断日志已上传", ToastType.SUCCESS)
                UploadDiagnosticLogsOutcome.CODE_NO_PENDING ->
                    LogUploadUiEvent("暂无待上传日志", ToastType.INFO)
                UploadDiagnosticLogsOutcome.CODE_ERROR ->
                    LogUploadUiEvent(outcome.message ?: "上传失败", ToastType.ERROR)
                else ->
                    LogUploadUiEvent("上传失败", ToastType.ERROR)
            }
        }
    }

    fun clearChangePasswordSuccess() {
        _changePasswordSuccess.value = false
    }

    fun clearFeedbackUiState() {
        _feedbackSuccess.value = false
        _feedbackError.value = null
    }

    /**
     * 提交用户反馈（需已登录；分类：`bug` / `suggestion` / `general`）。
     * 用户标识由服务端根据 Token 解析，无需客户端填写联系方式。
     */
    fun submitFeedback(content: String, category: String) {
        viewModelScope.launch {
            _feedbackSubmitting.value = true
            _feedbackError.value = null
            _feedbackSuccess.value = false
            val (ok, msg) = feedbackApiService.submitOutcome(content, category)
            _feedbackSubmitting.value = false
            if (ok) {
                _feedbackSuccess.value = true
            } else {
                _feedbackError.value = msg
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
            _themeMode.value = mode
        }
    }

    init {
        viewModelScope.launch {
            _themeMode.value = themeRepository.getThemeMode()
        }
        // Observe "unauthorized" events from network layer and force user back to login.
        viewModelScope.launch {
            AuthEvents.events.collect { e ->
                if (e is AuthEvent.Unauthorized) {
                    forceLogoutToLogin(e.message ?: "登录已失效，请重新登录")
                }
            }
        }
        // 检查登录状态
        checkLoginStatus()
        if (_isLoggedIn.value) {
            loadPhotos()
            loadConfigs()
            PushRegistrationAndroid.sync(getApplication())
        } else {
            viewModelScope.launch {
                WidgetSnapshotSync.publishFromPhotos(emptyList(), false, getApplication())
                HomeWidgetUpdater.updateAll(getApplication())
            }
        }
    }
    
    private fun checkLoginStatus() {
        _isLoggedIn.value = tokenManager.isLoggedIn()
    }

    /**
     * Whether the app currently has a usable cloud auth token (i.e., logged in).
     */
    fun hasCloudAuthToken(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Clear local auth session and switch UI to login screen.
     * Used by screens that require auth, and by network "unauthorized" handler.
     */
    fun clearLocalSessionAndShowLoginUi(message: String? = null) {
        forceLogoutToLogin(message)
    }

    private fun forceLogoutToLogin(message: String? = null) {
        if (!_isLoggedIn.value && !tokenManager.isLoggedIn()) {
            return
        }
        // Only perform local cleanup. Do NOT call logout endpoint, because token may already be invalid.
        tokenManager.clearTokens()
        container.analyticsTracker.clearPending()
        AnalyticsSession.refresh()

        _isLoggedIn.value = false
        _currentUser.value = null
        _authError.value = message

        val prefs = getApplication<Application>()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged_in", false).apply()

        viewModelScope.launch {
            WidgetSnapshotSync.publishFromPhotos(emptyList(), false, getApplication())
            HomeWidgetUpdater.updateAll(getApplication())
        }
    }

    private fun notifyAuthSessionStarted() {
        container.analyticsTracker.flushPending()
        AnalyticsSession.refresh()
    }

    fun trackAnalyticsPageView(page: String, fromPage: String?) {
        container.analyticsTracker.pageView(page, fromPage)
    }

    fun trackBottomNavClick(targetRoute: String, fromRoute: String) {
        val eventId = when (targetRoute) {
            Screen.Photos.route -> AnalyticsEventIds.BOTTOM_NAV_PHOTOS
            Screen.Albums.route -> AnalyticsEventIds.BOTTOM_NAV_ALBUMS
            Screen.Camera.route -> AnalyticsEventIds.BOTTOM_NAV_CAMERA
            Screen.Storage.route -> AnalyticsEventIds.BOTTOM_NAV_STORAGE
            Screen.Settings.route -> AnalyticsEventIds.BOTTOM_NAV_SETTINGS
            else -> return
        }
        container.analyticsTracker.click(
            page = targetRoute.toAnalyticsPage(),
            elementId = eventId,
            elementType = "tab",
            fromPage = fromRoute.toAnalyticsPage()
        )
    }

    fun trackClick(
        page: String,
        eventId: String,
        elementType: String,
        elementName: String? = null,
        fromPage: String? = null,
        position: Int? = null,
        extra: String? = null
    ) {
        container.analyticsTracker.click(
            page = page,
            elementId = eventId,
            elementType = elementType,
            elementName = elementName,
            fromPage = fromPage,
            position = position,
            extra = extra
        )
    }

    fun trackLoginSubmit() {
        container.analyticsTracker.click(
            page = AnalyticsPages.LOGIN,
            elementId = AnalyticsEventIds.LOGIN_SUBMIT,
            elementType = "button",
            elementName = "登录"
        )
    }

    fun trackLoginGoRegister() {
        container.analyticsTracker.click(
            page = AnalyticsPages.LOGIN,
            elementId = AnalyticsEventIds.LOGIN_GO_REGISTER,
            elementType = "button",
            elementName = "立即注册"
        )
    }

    fun trackRegisterSubmit() {
        container.analyticsTracker.click(
            page = AnalyticsPages.REGISTER,
            elementId = AnalyticsEventIds.REGISTER_SUBMIT,
            elementType = "button",
            elementName = "注册"
        )
    }

    fun trackRegisterGoLogin() {
        container.analyticsTracker.click(
            page = AnalyticsPages.REGISTER,
            elementId = AnalyticsEventIds.REGISTER_GO_LOGIN,
            elementType = "button",
            elementName = "去登录"
        )
    }
    
    /**
     * 发送邮箱验证码
     */
    fun sendEmailCode(email: String, type: String = "register") {
        viewModelScope.launch {
            _authError.value = null
            val result = authService.sendEmailCode(email, type)
            result.onError { exception, message ->
                _authError.value = message ?: "发送验证码失败"
                Log.e("AppViewModel", "Failed to send email code: ${message}", exception)
            }
        }
    }
    
    /**
     * 用户登录（邮箱 + 密码）
     */
    fun login(account: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            val request = LoginRequest(account = account, password = password)
            val result = authService.login(request)
            
            result.onSuccess { response ->
                // 保存Token
                tokenManager.saveAccessToken(response.accessToken)
                tokenManager.saveRefreshToken(response.refreshToken)
                _currentUser.value = response.user
                
                // 先更新登录状态，确保界面能立即响应
                _isLoggedIn.value = true
                notifyAuthSessionStarted()
                Log.i("AppViewModel", "Login successful: ${response.user.username}, isLoggedIn set to true")
                
                // 保存登录状态到SharedPreferences（兼容旧代码）
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_logged_in", true).apply()
                
                loadPhotos()
                loadConfigs()
                PushRegistrationAndroid.sync(getApplication())
            }.onError { exception, message ->
                val errorMsg = message ?: exception.message ?: "登录失败"
                _authError.value = errorMsg
                Log.e("AppViewModel", "Login failed: $errorMsg", exception)
                exception.printStackTrace() // 打印完整堆栈信息
            }
        }
    }
    
    /**
     * 用户注册
     */
    fun register(username: String, email: String, password: String, emailCode: String) {
        viewModelScope.launch {
            _authError.value = null
            val request = RegisterRequest(
                username = username,
                email = email,
                password = password,
                emailCode = emailCode
            )
            val result = authService.register(request)
            
            result.onSuccess { user ->
                _currentUser.value = user
                // 注册成功后自动登录（使用邮箱作为账号）
                login(email, password)
                Log.i("AppViewModel", "Register successful: ${user.username}")
            }.onError { exception, message ->
                _authError.value = message ?: "注册失败"
                Log.e("AppViewModel", "Register failed: ${message}", exception)
            }
        }
    }
    
    /**
     * 清除认证错误（UI 展示 Toast 后调用）
     */
    fun clearAuthError() {
        _authError.value = null
    }

    /**
     * 更新个人资料（用户名、头像等）
     */
    fun updateProfile(username: String?, avatar: String? = null) {
        viewModelScope.launch {
            _authError.value = null
            val token = tokenManager.getAccessToken() ?: run {
                _authError.value = "请先登录"
                return@launch
            }
            val request = UpdateProfileRequest(username = username, avatar = avatar)
            val result = authService.updateProfile(token, request)
            result.onSuccess { user ->
                _currentUser.value = user
                _authError.value = null
            }.onError { exception, message ->
                _authError.value = message ?: exception.message ?: "更新失败"
                Log.e("AppViewModel", "Update profile failed: ${message}", exception)
            }
        }
    }

    /**
     * 修改密码（需先通过邮箱验证码验证）
     */
    fun changePassword(email: String, emailCode: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authError.value = null
            val request = ChangePasswordRequest(
                email = email,
                emailCode = emailCode,
                oldPassword = oldPassword,
                newPassword = newPassword
            )
            val result = authService.changePassword(request)
            result.onSuccess {
                _authError.value = null
                _changePasswordSuccess.value = true
            }.onError { exception, message ->
                _authError.value = message ?: exception.message ?: "修改密码失败"
                Log.e("AppViewModel", "Change password failed: ${message}", exception)
            }
        }
    }
    
    /**
     * 用户登出
     */
    fun logout() {
        viewModelScope.launch {
            runCatching {
                if (tokenManager.isLoggedIn()) {
                    val installId = pushInstallId(getApplication())
                    container.pushDeviceApiService.unregisterOutcome(installId)
                }
            }
            val accessToken = tokenManager.getAccessToken()
            val refreshToken = tokenManager.getRefreshToken()
            
            if (accessToken != null) {
                authService.logout(accessToken, refreshToken)
            }
            
            // 清除Token
            tokenManager.clearTokens()
            container.analyticsTracker.clearPending()
            AnalyticsSession.refresh()
            _isLoggedIn.value = false
            _currentUser.value = null
            
            // 清除登录状态
            val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", false).apply()

            WidgetSnapshotSync.publishFromPhotos(emptyList(), false, getApplication())
            HomeWidgetUpdater.updateAll(getApplication())
        }
    }
    
    fun loadPhotos() {
        viewModelScope.launch {
            _photos.value = photoService.getAllPhotos()
            WidgetSnapshotSync.publishFromPhotos(_photos.value, _isLoggedIn.value, getApplication())
            HomeWidgetUpdater.updateAll(getApplication())
        }
    }
    
    fun loadConfigs() {
        viewModelScope.launch {
            // 如果已登录，从后端加载配置
            if (_isLoggedIn.value) {
                val result = configApiService.getConfigs()
                result.onSuccess { configDTOs ->
                    // 转换为本地模型
                    val configs = configDTOs.map { it.toStorageConfig() }
                    _configs.value = configs
                    
                    // 查找默认配置
                    _defaultConfig.value = configs.firstOrNull { it.isDefault }
                    
                    // 同时保存到本地缓存
                    configs.forEach { config ->
                        configService.saveConfig(config)
                    }
                }.onError { exception, message ->
                    Log.e("AppViewModel", "Failed to load configs from API: $message", exception)
                    // 如果API失败，从本地加载
                    _configs.value = configService.getAllConfigs()
                    _defaultConfig.value = configService.getDefaultConfig()
                }
            } else {
                // 未登录时从本地加载
                _configs.value = configService.getAllConfigs()
                _defaultConfig.value = configService.getDefaultConfig()
            }
        }
    }
    
    fun uploadPhoto(photoData: ByteArray, fileName: String, mimeType: String, width: Int, height: Int) {
        viewModelScope.launch {
            // 使用后端 API 上传（后端中转）
            val result = container.photoApiService.uploadPhoto(
                photoData = photoData,
                fileName = fileName,
                contentType = mimeType,
                albumId = null,
                takenAt = null
            )
            result.onSuccess { photoDTO ->
                Log.i("AppViewModel", "Photo uploaded successfully: ${photoDTO.id}")
                loadPhotos()
            }.onError { exception, message ->
                val appError = ErrorHandler.handleError(exception)
                Log.e("AppViewModel", "Failed to upload photo: ${appError.message}", exception)
            }
        }
    }
    
    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            photoService.deletePhoto(photoId).onSuccess {
                Log.i("AppViewModel", "Photo deleted successfully: $photoId")
                loadPhotos()
            }.onFailure { error ->
                val appError = ErrorHandler.handleError(error)
                Log.e("AppViewModel", "Failed to delete photo: ${appError.message}", error)
            }
        }
    }
    
    fun saveConfig(config: StorageConfig) {
        viewModelScope.launch {
            // 如果已登录，保存到后端
            if (_isLoggedIn.value) {
                val configDTO = config.toCloudConfigDTO()
                val result = configApiService.saveConfig(configDTO)
                result.onSuccess { savedConfigDTO ->
                    // 保存成功，转换为本地模型并更新UI
                    val savedConfig = savedConfigDTO.toStorageConfig()
                    
                    // 如果设为默认，激活配置
                    if (config.isDefault) {
                        savedConfigDTO.id?.let { configId ->
                            configApiService.activateConfig(configId)
                        }
                    }
                    
                    // 同时保存到本地缓存
                    configService.saveConfig(savedConfig)
                    
                    // 重新加载配置列表
                    loadConfigs()
                    
                    Log.i("AppViewModel", "Config saved to backend: ${savedConfig.id}")
                }.onError { exception, message ->
                    Log.e("AppViewModel", "Failed to save config to backend: $message", exception)
                    _authError.value = message ?: "保存配置失败，请检查网络连接"
                    
                    // 如果后端保存失败，仍然保存到本地（离线支持）
                    configService.saveConfig(config)
                    loadConfigs()
                }
            } else {
                // 未登录时只保存到本地
                configService.saveConfig(config)
                loadConfigs()
            }
        }
    }
    
    fun deleteConfig(configId: String) {
        viewModelScope.launch {
            // 如果已登录，从后端删除
            if (_isLoggedIn.value) {
                val result = configApiService.deleteConfig(configId)
                result.onSuccess {
                    // 后端删除成功，同时删除本地缓存
                    configService.deleteConfig(configId)
                    loadConfigs()
                    Log.i("AppViewModel", "Config deleted from backend: $configId")
                }.onError { exception, message ->
                    Log.e("AppViewModel", "Failed to delete config from backend: $message", exception)
                    _authError.value = message ?: "删除配置失败"
                }
            } else {
                // 未登录时只删除本地
                configService.deleteConfig(configId)
                loadConfigs()
            }
        }
    }
    
    fun setDefaultConfig(configId: String) {
        viewModelScope.launch {
            // 如果已登录，在后端激活配置
            if (_isLoggedIn.value) {
                val result = configApiService.activateConfig(configId)
                result.onSuccess {
                    // 后端激活成功，同时更新本地
                    configService.setDefaultConfig(configId)
                    loadConfigs()
                    Log.i("AppViewModel", "Config activated on backend: $configId")
                }.onError { exception, message ->
                    Log.e("AppViewModel", "Failed to activate config on backend: $message", exception)
                    _authError.value = message ?: "设置默认配置失败"
                }
            } else {
                // 未登录时只更新本地
                configService.setDefaultConfig(configId)
                loadConfigs()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 容器会在应用退出时统一清理
    }
}

