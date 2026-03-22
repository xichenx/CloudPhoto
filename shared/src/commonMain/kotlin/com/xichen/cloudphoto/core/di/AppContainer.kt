package com.xichen.cloudphoto.core.di

import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.NetworkClientFactory
import com.xichen.cloudphoto.core.network.NetworkConfig
import com.xichen.cloudphoto.core.theme.ThemeRepository
import com.xichen.cloudphoto.repository.AlbumRepository
import com.xichen.cloudphoto.repository.ConfigRepository
import com.xichen.cloudphoto.repository.PhotoRepository
import com.xichen.cloudphoto.service.AlbumApiService
import com.xichen.cloudphoto.service.AlbumService
import com.xichen.cloudphoto.service.AuthService
import com.xichen.cloudphoto.service.ConfigApiService
import com.xichen.cloudphoto.service.ConfigService
import com.xichen.cloudphoto.core.logger.RemoteLogUploadScheduler
import com.xichen.cloudphoto.analytics.AnalyticsDeviceMeta
import com.xichen.cloudphoto.analytics.AnalyticsTracker
import com.xichen.cloudphoto.analytics.analyticsDeviceMeta
import com.xichen.cloudphoto.service.AppEventApiService
import com.xichen.cloudphoto.service.PhotoApiService
import com.xichen.cloudphoto.service.PhotoService
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

/**
 * 应用依赖容器
 * 统一管理所有依赖的创建和生命周期
 */
class AppContainer(context: Any? = null) {

    private val appContext: Any? = context
    // Token Manager
    val tokenManager: TokenManager by lazy { 
        TokenManager(context)
    }
    
    // Repositories (保留用于本地存储或缓存)
    val configRepository: ConfigRepository by lazy { ConfigRepository() }
    val photoRepository: PhotoRepository by lazy { PhotoRepository() }
    val albumRepository: AlbumRepository by lazy { AlbumRepository() }
    val themeRepository: ThemeRepository by lazy { ThemeRepository() }
    
    // Network (用于对象存储直传，与各云厂商 endpoint 不同，单独客户端)
    private val httpClient: HttpClient by lazy {
        NetworkClientFactory.create(
            baseUrl = null,
            timeout = NetworkConfig.API_TIMEOUT,
            enableLogging = true
        )
    }

    /** 业务 API：登录/注册等，不在默认头中附带 Token */
    private val authApiHttpClient = lazy {
        NetworkClientFactory.create(
            baseUrl = ApiConfig.AUTH_BASE_URL,
            timeout = NetworkConfig.API_TIMEOUT,
            enableLogging = true,
            tokenManager = null
        )
    }

    /** 业务 API：需鉴权的接口，与 Photo/Album/Config 共用，减少重复连接与配置 */
    private val authorizedApiHttpClient = lazy {
        NetworkClientFactory.create(
            baseUrl = ApiConfig.AUTH_BASE_URL,
            timeout = NetworkConfig.API_TIMEOUT,
            enableLogging = true,
            tokenManager = tokenManager
        )
    }

    private val analyticsJob: Job = SupervisorJob()

    private val analyticsScope: CoroutineScope = CoroutineScope(analyticsJob + Dispatchers.Default)

    /** 埋点（仅在有 Token 时上报） */
    val analyticsTracker: AnalyticsTracker by lazy {
        val meta: AnalyticsDeviceMeta = analyticsDeviceMeta(appContext)
        AnalyticsTracker(
            api = AppEventApiService(authorizedApiHttpClient.value),
            tokenManager = tokenManager,
            deviceMeta = meta,
            scope = analyticsScope
        )
    }
    
    // Services (本地服务，用于对象存储直接上传)
    val configService: ConfigService by lazy {
        ConfigService(configRepository)
    }
    
    val photoService: PhotoService by lazy {
        PhotoService(photoRepository, configRepository, httpClient)
    }
    
    val albumService: AlbumService by lazy {
        AlbumService(albumRepository, photoRepository)
    }
    
    // API Services (调用后端 API)
    /** 认证服务（登录、注册、验证码等），baseUrl 由 shared ApiConfig 统一配置 */
    val authService: AuthService by lazy {
        AuthService(authApiHttpClient.value)
    }
    
    /** 照片 API 服务（调用后端 API） */
    val photoApiService: PhotoApiService by lazy {
        PhotoApiService(authorizedApiHttpClient.value)
    }
    
    /** 相册 API 服务（调用后端 API） */
    val albumApiService: AlbumApiService by lazy {
        AlbumApiService(authorizedApiHttpClient.value)
    }
    
    /** 对象存储配置 API 服务（调用后端 API） */
    val configApiService: ConfigApiService by lazy {
        ConfigApiService(authorizedApiHttpClient.value)
    }

    /**
     * 启动诊断日志定时上传（需先 [com.xichen.cloudphoto.core.logger.DiagnosticLogging.install]）。
     */
    fun startDiagnosticLogUpload() {
        RemoteLogUploadScheduler.start(authorizedApiHttpClient.value)
    }

    /**
     * 清理资源
     */
    fun dispose() {
        RemoteLogUploadScheduler.stop()
        analyticsJob.cancel()
        httpClient.close()
        if (authApiHttpClient.isInitialized()) {
            authApiHttpClient.value.close()
        }
        if (authorizedApiHttpClient.isInitialized()) {
            authorizedApiHttpClient.value.close()
        }
    }
}

/**
 * 全局容器实例
 */
object AppContainerHolder {
    private var container: AppContainer? = null
    
    fun getContainer(context: Any? = null): AppContainer {
        if (container == null) {
            container = AppContainer(context)
        }
        return container!!
    }
    
    fun dispose() {
        container?.dispose()
        container = null
    }
}

