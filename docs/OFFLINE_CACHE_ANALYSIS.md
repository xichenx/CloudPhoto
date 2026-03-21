# 断网环境下缓存查看实现方案分析

## 当前状态分析

### ✅ 已实现的缓存机制

#### 1. 照片元数据缓存
- **Android**: 使用 `SharedPreferences` 存储 `Photo` 对象（JSON 序列化）
- **iOS**: 使用 `NSUserDefaults` 存储 `Photo` 对象（JSON 序列化）
- **存储位置**: `shared/src/androidMain/.../PhotoRepository.android.kt` 和 `shared/src/iosMain/.../PhotoRepository.ios.kt`
- **数据持久化**: ✅ 应用重启后数据保留

```kotlin
// PhotoRepository 已实现的方法
suspend fun savePhoto(photo: Photo)      // 保存照片元数据
suspend fun getAllPhotos(): List<Photo>   // 获取所有照片（从本地）
suspend fun getPhoto(id: String): Photo?  // 获取单张照片
```

#### 2. 配置信息缓存
- **ConfigRepository**: 同样使用 SharedPreferences/NSUserDefaults
- **ConfigService.loadConfigs()**: API 失败时自动降级到本地缓存

```kotlin:298:327:composeApp/src/androidMain/kotlin/com/xichen/cloudphoto/AppViewModel.kt
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
```

### ❌ 缺失的缓存机制

#### 1. 图片数据持久化缓存
**问题**: 当前只有内存缓存，应用重启后图片数据丢失

```kotlin:36:64:shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/image/ImageLoader.kt
// 简单的内存缓存
private val memoryCache = mutableMapOf<String, ByteArray>()
private val maxCacheSize = 50 * 1024 * 1024 // 50MB
private var currentCacheSize = 0L

override suspend fun loadImageWithCache(url: String): ImageLoadResult = withContext(Dispatchers.IO) {
    // 先检查缓存
    memoryCache[url]?.let {
        return@withContext ImageLoadResult.Success(it)
    }
    
    // 从网络加载
    loadImage(url).let { result ->
        if (result is ImageLoadResult.Success) {
            // 添加到缓存
            addToCache(url, result.data)
        }
        result
    }
}
```

**影响**: 
- 断网时无法查看已下载过的图片
- 应用重启后需要重新下载所有图片

#### 2. 网络状态检测
**问题**: 没有统一的网络状态管理机制

**当前错误处理**:
```kotlin:39:61:shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/error/ErrorHandler.kt
fun handleError(error: Throwable): AppError {
    Log.e("ErrorHandler", "Error occurred", error)
    
    return when {
        error.message?.contains("network", ignoreCase = true) == true ||
        error.message?.contains("connection", ignoreCase = true) == true ||
        error.message?.contains("timeout", ignoreCase = true) == true ||
        error.message?.contains("host", ignoreCase = true) == true -> {
            AppError.NetworkError("网络连接失败，请检查网络设置", error)
        }
        // ...
    }
}
```

**影响**: 
- 无法主动检测网络状态
- 只能被动处理网络错误

#### 3. 图片加载降级策略
**问题**: UI 层直接使用 HttpClient，失败后无本地缓存回退

```kotlin:207:224:composeApp/src/androidMain/kotlin/com/xichen/cloudphoto/ui/PhotosScreen.kt
LaunchedEffect(photo.url) {
    scope.launch {
        try {
            isLoading = true
            val httpClient = HttpClient()
            val bytes = httpClient.get(photo.url).body<ByteArray>()
            val bitmap = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            imageBitmap = bitmap
            isLoading = false
            httpClient.close()
        } catch (e: Exception) {
            isLoading = false
            e.printStackTrace()
        }
    }
}
```

**影响**: 
- 断网时图片无法显示
- 即使之前下载过也无法从本地加载

## 实现方案

### 方案一：完整离线缓存方案（推荐）

#### 1. 图片磁盘缓存层

**架构设计**:
```
┌─────────────────────────────────────────┐
│          UI Layer (Compose/SwiftUI)     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      ImageLoader (统一接口)              │
│  - loadImageWithCache(url)               │
│  - 优先从磁盘缓存加载                     │
│  - 失败时降级到内存缓存                    │
└─────────────────┬───────────────────────┘
                  │
      ┌───────────┴───────────┐
      │                       │
┌─────▼──────┐      ┌─────────▼──────────┐
│ DiskCache  │      │  NetworkLoader      │
│ (持久化)    │      │  (网络请求)         │
└────────────┘      └────────────────────┘
```

**实现步骤**:

1. **创建磁盘缓存接口** (`shared/src/commonMain/.../DiskImageCache.kt`):
```kotlin
expect class DiskImageCache {
    suspend fun get(url: String): ByteArray?
    suspend fun put(url: String, data: ByteArray)
    suspend fun clear()
    suspend fun getCacheSize(): Long
}
```

2. **Android 实现** (`shared/src/androidMain/.../DiskImageCache.android.kt`):
```kotlin
actual class DiskImageCache(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "image_cache")
    
    actual suspend fun get(url: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, urlToFileName(url))
        if (file.exists()) file.readBytes() else null
    }
    
    actual suspend fun put(url: String, data: ByteArray) = withContext(Dispatchers.IO) {
        cacheDir.mkdirs()
        val file = File(cacheDir, urlToFileName(url))
        file.writeBytes(data)
    }
    
    private fun urlToFileName(url: String): String {
        return url.hashCode().toString() + ".cache"
    }
}
```

3. **iOS 实现** (`shared/src/iosMain/.../DiskImageCache.ios.kt`):
```kotlin
actual class DiskImageCache {
    private val cacheDir = NSFileManager.defaultManager
        .URLForDirectory(
            NSCachesDirectory,
            inDomains: .UserDomainMask,
            appropriateForURL: null,
            create: true,
            error: null
        )?.appendingPathComponent("image_cache")
    
    actual suspend fun get(url: String): ByteArray? = withContext(Dispatchers.Default) {
        val fileName = url.hashCode().toString() + ".cache"
        val fileURL = cacheDir?.appendingPathComponent(fileName)
        NSData.dataWithContentsOfURL(fileURL)?.toByteArray()
    }
    
    actual suspend fun put(url: String, data: ByteArray) = withContext(Dispatchers.Default) {
        val fileName = url.hashCode().toString() + ".cache"
        val fileURL = cacheDir?.appendingPathComponent(fileName)
        data.toNSData()?.writeToURL(fileURL, atomically: true)
    }
}
```

4. **增强 ImageLoader**:
```kotlin
class CachedImageLoader(
    private val httpClient: HttpClient,
    private val diskCache: DiskImageCache
) : ImageLoader {
    
    override suspend fun loadImageWithCache(url: String): ImageLoadResult = withContext(Dispatchers.IO) {
        // 1. 先检查磁盘缓存
        diskCache.get(url)?.let {
            return@withContext ImageLoadResult.Success(it)
        }
        
        // 2. 尝试网络加载
        try {
            val data = httpClient.get(url).body<ByteArray>()
            // 3. 保存到磁盘缓存
            diskCache.put(url, data)
            ImageLoadResult.Success(data)
        } catch (e: Exception) {
            ImageLoadResult.Error(e)
        }
    }
}
```

#### 2. 网络状态检测

**创建网络状态管理器** (`shared/src/commonMain/.../NetworkMonitor.kt`):
```kotlin
expect class NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isConnected(): Boolean
}
```

**Android 实现**:
```kotlin
actual class NetworkMonitor(private val context: Context) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    
    actual val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        connectivityManager?.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager?.unregisterNetworkCallback(callback) }
    }
    
    actual fun isConnected(): Boolean {
        return connectivityManager?.activeNetwork != null
    }
}
```

**iOS 实现**:
```kotlin
actual class NetworkMonitor {
    private let reachability = SCNetworkReachabilityCreateWithName(nil, "www.apple.com")
    
    actual val isOnline: Flow<Boolean> = callbackFlow {
        // 使用 Network framework 或 Reachability
        // ...
    }
}
```

#### 3. 离线优先的图片加载策略

**更新 PhotosScreen**:
```kotlin
@Composable
fun ModernPhotoItem(photo: Photo, viewModel: AppViewModel) {
    val imageLoader = remember { AppContainerHolder.getContainer().imageLoader }
    val networkMonitor = remember { AppContainerHolder.getContainer().networkMonitor }
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(photo.url) {
        scope.launch {
            isLoading = true
            val result = imageLoader.loadImageWithCache(photo.url)
            
            when (result) {
                is ImageLoadResult.Success -> {
                    imageBitmap = BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
                    isLoading = false
                }
                is ImageLoadResult.Error -> {
                    // 离线提示
                    if (!isOnline) {
                        // 显示离线占位符
                    }
                    isLoading = false
                }
                else -> {}
            }
        }
    }
    
    // UI 渲染...
}
```

### 方案二：轻量级方案（快速实现）

如果不想引入磁盘缓存，可以快速实现以下改进：

#### 1. 增强 PhotoRepository - 保存图片数据
```kotlin
// 在 PhotoRepository 中添加图片数据存储
actual suspend fun savePhotoData(photoId: String, imageData: ByteArray) = withContext(Dispatchers.IO) {
    val prefs = prefs ?: return@withContext
    val base64 = Base64.encodeToString(imageData, Base64.NO_WRAP)
    prefs.edit().putString("photo_data_$photoId", base64).apply()
}

actual suspend fun getPhotoData(photoId: String): ByteArray? = withContext(Dispatchers.IO) {
    val prefs = prefs ?: return@withContext null
    val base64 = prefs.getString("photo_data_$photoId", null) ?: return@withContext null
    Base64.decode(base64, Base64.NO_WRAP)
}
```

#### 2. 图片加载时优先使用本地数据
```kotlin
LaunchedEffect(photo.url) {
    scope.launch {
        isLoading = true
        
        // 1. 先尝试从本地加载
        val localData = photoRepository.getPhotoData(photo.id)
        if (localData != null) {
            imageBitmap = BitmapFactory.decodeByteArray(localData, 0, localData.size)
            isLoading = false
            return@launch
        }
        
        // 2. 网络加载
        try {
            val httpClient = HttpClient()
            val bytes = httpClient.get(photo.url).body<ByteArray>()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageBitmap = bitmap
            
            // 3. 保存到本地
            photoRepository.savePhotoData(photo.id, bytes)
            
            isLoading = false
            httpClient.close()
        } catch (e: Exception) {
            isLoading = false
            // 显示离线提示
        }
    }
}
```

**优点**: 
- 实现简单，改动小
- 利用现有的 PhotoRepository 机制

**缺点**: 
- Base64 编码增加存储空间（约 33%）
- SharedPreferences 不适合存储大文件（建议 < 1MB）
- 性能不如文件系统缓存

## 推荐实现路径

### 阶段一：快速改进（1-2 天）
1. ✅ 在 `PhotoRepository` 中添加图片数据存储（使用文件系统，而非 SharedPreferences）
2. ✅ 更新 `PhotosScreen` 图片加载逻辑，优先从本地加载
3. ✅ 添加离线状态 UI 提示

### 阶段二：完善缓存（3-5 天）
1. ✅ 实现 `DiskImageCache` expect/actual
2. ✅ 增强 `ImageLoader` 支持磁盘缓存
3. ✅ 添加缓存大小管理和清理机制

### 阶段三：网络状态管理（2-3 天）
1. ✅ 实现 `NetworkMonitor` expect/actual
2. ✅ 在 UI 层显示网络状态
3. ✅ 优化离线体验（禁用上传、显示提示等）

## 关键代码位置

### 需要修改的文件

1. **图片加载**:
   - `composeApp/src/androidMain/kotlin/com/xichen/cloudphoto/ui/PhotosScreen.kt` (第 207-224 行)
   - `iosApp/iosApp/Views/Photos/PhotosView.swift` (第 80 行)

2. **缓存层**:
   - `shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/image/ImageLoader.kt`
   - 新建: `shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/image/DiskImageCache.kt`

3. **网络状态**:
   - 新建: `shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/network/NetworkMonitor.kt`

4. **依赖注入**:
   - `shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/di/AppContainer.kt`

## 注意事项

1. **存储空间管理**: 
   - 设置缓存大小上限（如 500MB）
   - 实现 LRU 淘汰策略
   - 提供手动清理缓存功能

2. **安全性**:
   - 缓存文件使用应用私有目录
   - 敏感图片考虑加密存储

3. **性能优化**:
   - 图片加载使用协程，避免阻塞 UI
   - 大图片考虑缩略图缓存
   - 使用图片压缩减少存储空间

4. **用户体验**:
   - 离线时显示明确提示
   - 已缓存的图片立即显示
   - 网络恢复后自动同步
