package com.xichen.cloudphoto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xichen.cloudphoto.model.Photo
import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.core.di.AppContainerHolder
import com.xichen.cloudphoto.core.error.ErrorHandler
import com.xichen.cloudphoto.core.logger.Log
import com.xichen.cloudphoto.service.AlbumService
import com.xichen.cloudphoto.service.AuthService
import com.xichen.cloudphoto.service.ConfigService
import com.xichen.cloudphoto.service.PhotoService
import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.network.*
import com.xichen.cloudphoto.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    // 使用依赖注入容器
    private val container = AppContainerHolder.getContainer()
    
    // 初始化 Repositories
    init {
        container.configRepository.init(application)
        container.photoRepository.init(application)
        container.albumRepository.init(application)
    }
    
    // 从容器获取服务（含认证服务，接口 baseUrl 在 shared ApiConfig 中统一配置）
    private val configService: ConfigService = container.configService
    private val photoService: PhotoService = container.photoService
    private val albumService: AlbumService = container.albumService
    private val authService: AuthService = container.authService
    
    // Token管理器 - 使用 Context 构造函数
    private val tokenManager: TokenManager = TokenManager(application.applicationContext)
    
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
    
    init {
        // 检查登录状态
        checkLoginStatus()
        if (_isLoggedIn.value) {
            loadPhotos()
            loadConfigs()
        }
    }
    
    private fun checkLoginStatus() {
        _isLoggedIn.value = tokenManager.isLoggedIn()
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
     * 用户登录（支持邮箱或手机号）
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
                Log.i("AppViewModel", "Login successful: ${response.user.username}, isLoggedIn set to true")
                
                // 保存登录状态到SharedPreferences（兼容旧代码）
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_logged_in", true).apply()
                
                loadPhotos()
                loadConfigs()
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
    fun register(username: String, email: String, password: String, emailCode: String, phone: String? = null) {
        viewModelScope.launch {
            _authError.value = null
            val request = RegisterRequest(
                username = username,
                email = email,
                password = password,
                phone = phone,
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
     * Mock 登录 - 用于开发和测试，直接设置登录状态和 mock 用户数据
     */
    fun mockLogin() {
        viewModelScope.launch {
            // 创建 mock 用户数据
            val mockUser = UserDTO(
                id = "mock_user_001",
                username = "测试用户",
                email = "test@example.com",
                phone = "13800138000",
                role = "user",
                avatar = null,
                createdAt = System.currentTimeMillis()
            )
            
            // 保存 mock token（仅用于本地状态，不会发送到服务器）
            tokenManager.saveAccessToken("mock_access_token_${System.currentTimeMillis()}")
            tokenManager.saveRefreshToken("mock_refresh_token_${System.currentTimeMillis()}")
            
            // 设置用户信息和登录状态
            _currentUser.value = mockUser
            _isLoggedIn.value = true
            
            // 保存登录状态到SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", true).apply()
            
            Log.i("AppViewModel", "Mock login successful: ${mockUser.username}")
            
            // 加载数据
            loadPhotos()
            loadConfigs()
        }
    }
    
    /**
     * 用户登出
     */
    fun logout() {
        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken()
            val refreshToken = tokenManager.getRefreshToken()
            
            if (accessToken != null) {
                authService.logout(accessToken, refreshToken)
            }
            
            // 清除Token
            tokenManager.clearTokens()
            _isLoggedIn.value = false
            _currentUser.value = null
            
            // 清除登录状态
            val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", false).apply()
        }
    }
    
    fun loadPhotos() {
        viewModelScope.launch {
            _photos.value = photoService.getAllPhotos()
        }
    }
    
    fun loadConfigs() {
        viewModelScope.launch {
            _configs.value = configService.getAllConfigs()
            _defaultConfig.value = configService.getDefaultConfig()
        }
    }
    
    fun uploadPhoto(photoData: ByteArray, fileName: String, mimeType: String, width: Int, height: Int) {
        viewModelScope.launch {
            val result = photoService.uploadPhoto(
                photoData = photoData,
                fileName = fileName,
                mimeType = mimeType,
                width = width,
                height = height
            )
            result.onSuccess {
                Log.i("AppViewModel", "Photo uploaded successfully: ${it.id}")
                loadPhotos()
            }.onFailure { error ->
                val appError = ErrorHandler.handleError(error)
                Log.e("AppViewModel", "Failed to upload photo: ${appError.message}", error)
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
            configService.saveConfig(config)
            loadConfigs()
        }
    }
    
    fun deleteConfig(configId: String) {
        viewModelScope.launch {
            configService.deleteConfig(configId)
            loadConfigs()
        }
    }
    
    fun setDefaultConfig(configId: String) {
        viewModelScope.launch {
            configService.setDefaultConfig(configId)
            loadConfigs()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 容器会在应用退出时统一清理
    }
}

