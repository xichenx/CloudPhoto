package com.xichen.cloudphoto.core.di

import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.network.NetworkClientFactory
import com.xichen.cloudphoto.core.theme.ThemeRepository
import com.xichen.cloudphoto.repository.AlbumRepository
import com.xichen.cloudphoto.repository.ConfigRepository
import com.xichen.cloudphoto.repository.PhotoRepository
import com.xichen.cloudphoto.service.AlbumApiService
import com.xichen.cloudphoto.service.AlbumService
import com.xichen.cloudphoto.service.AuthService
import com.xichen.cloudphoto.service.ConfigApiService
import com.xichen.cloudphoto.service.ConfigService
import com.xichen.cloudphoto.service.PhotoApiService
import com.xichen.cloudphoto.service.PhotoService
import io.ktor.client.HttpClient

/**
 * 应用依赖容器
 * 统一管理所有依赖的创建和生命周期
 */
class AppContainer(context: Any? = null) {
    // Token Manager
    val tokenManager: TokenManager by lazy { 
        TokenManager(context)
    }
    
    // Repositories (保留用于本地存储或缓存)
    val configRepository: ConfigRepository by lazy { ConfigRepository() }
    val photoRepository: PhotoRepository by lazy { PhotoRepository() }
    val albumRepository: AlbumRepository by lazy { AlbumRepository() }
    val themeRepository: ThemeRepository by lazy { ThemeRepository() }
    
    // Network (用于对象存储直接上传)
    private val httpClient: HttpClient by lazy {
        NetworkClientFactory.create(
            baseUrl = null, // 对象存储使用不同的 endpoint
            timeout = 60_000L, // 上传大文件需要更长时间
            enableLogging = true
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
        AuthService(baseUrl = ApiConfig.AUTH_BASE_URL)
    }
    
    /** 照片 API 服务（调用后端 API） */
    val photoApiService: PhotoApiService by lazy {
        PhotoApiService(tokenManager)
    }
    
    /** 相册 API 服务（调用后端 API） */
    val albumApiService: AlbumApiService by lazy {
        AlbumApiService(tokenManager)
    }
    
    /** 对象存储配置 API 服务（调用后端 API） */
    val configApiService: ConfigApiService by lazy {
        ConfigApiService(tokenManager)
    }
    
    /**
     * 清理资源
     */
    fun dispose() {
        httpClient.close()
        photoApiService.close()
        albumApiService.close()
        configApiService.close()
        authService.close()
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

