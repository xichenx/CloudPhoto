# 云相册应用 - 基础架构文档

## 架构概览

本项目采用 Kotlin Multiplatform (KMP) 架构，共享业务逻辑，平台特定 UI。

```text
CloudPhoto/
├── shared/                    # KMP 共享模块
│   ├── core/                  # 核心框架
│   │   ├── network/          # 网络请求框架
│   │   ├── theme/             # 主题框架
│   │   ├── ui/                # UI 工具（设备信息等）
│   │   ├── di/                # 依赖注入
│   │   ├── logger/            # 日志框架
│   │   ├── image/             # 图片加载
│   │   ├── error/             # 错误处理
│   │   ├── config/            # 配置管理
│   │   ├── permission/        # 权限管理
│   │   └── utils/             # 工具类
│   ├── model/                 # 数据模型
│   ├── repository/            # 数据仓库
│   └── service/               # 业务服务
├── composeApp/                # Android 应用
│   ├── navigation/            # 路由框架
│   ├── theme/                 # 主题实现
│   ├── core/                  # 核心工具（Toast等）
│   └── ui/                    # UI 组件
└── iosApp/                    # iOS 应用
    ├── Navigation/            # 路由框架
    ├── Theme/                 # 主题实现
    └── Core/                  # 核心工具
```

## 1. 网络请求框架

### 1.1 ApiResult - 统一响应封装

```kotlin
// 使用示例
val result: ApiResult<User> = httpClient.get("/api/user")
result.onSuccess { user ->
    // 处理成功
}.onError { exception, message ->
    // 处理错误
}
```

### 1.2 NetworkClientFactory - 网络客户端工厂

```kotlin
// 创建网络客户端
val httpClient = NetworkClientFactory.create(
    baseUrl = "https://api.example.com",
    timeout = 30_000L,
    enableLogging = true
)

// 使用扩展函数进行请求
val result: ApiResult<User> = httpClient.get("/api/user") {
    header("Authorization", "Bearer $token")
}
```

### 1.3 特性

- ✅ 统一的错误处理
- ✅ 自动 JSON 序列化/反序列化
- ✅ 请求超时配置
- ✅ 日志记录
- ✅ 重试机制（可扩展）

## 2. 路由框架

### 2.1 Android (Compose Navigation)

```kotlin
// 定义路由
sealed class Screen(val route: String) {
    object Photos : Screen("photos")
    object Albums : Screen("albums")
    object Settings : Screen("settings")
}

// 导航
navController.navigate(Screen.Photos.route)

// 导航图
NavGraph(
    navController = navController,
    viewModel = viewModel
)
```

### 2.2 iOS (SwiftUI Navigation)

```swift
// 使用 NavigationCoordinator
@StateObject private var coordinator = NavigationCoordinator()

// 导航
coordinator.navigate(to: .photos)

// 路由容器
NavigationContainer { coordinator in
    // 根据 coordinator.currentRoute 显示不同视图
}
```

## 3. 主题框架

### 3.1 共享主题配置

```kotlin
// 主题模式
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

// 主题颜色
data class AppColors(...)
object DefaultTheme {
    val lightColors: AppColors
    val darkColors: AppColors
}
```

### 3.2 Android 实现

```kotlin
@Composable
fun App() {
    CloudPhotoTheme(themeMode = ThemeMode.SYSTEM) {
        // 应用内容
    }
}
```

### 3.3 iOS 实现

```swift
ThemedView {
    // 应用内容
}
```

### 3.4 主题持久化

```kotlin
val themeRepository = ThemeRepository()
themeRepository.setThemeMode(ThemeMode.DARK)
val currentMode = themeRepository.getThemeMode()
```

## 4. 多设备适配

### 4.1 设备信息

```kotlin
// 共享接口
expect class DeviceInfo {
    val deviceType: DeviceType
    val screenSize: ScreenSize
    val orientation: ScreenOrientation
    // ...
}
```

### 4.2 Android 响应式布局

```kotlin
@Composable
fun MyScreen() {
    ResponsiveContainer {
        val config = rememberResponsiveConfig()
        
        if (config.isTablet) {
            // 平板布局
            Row {
                // 侧边栏 + 主内容
            }
        } else {
            // 手机布局
            Column {
                // 单列布局
            }
        }
    }
}
```

### 4.3 iOS 响应式布局

```swift
struct MyView: View {
    @Environment(\.responsiveConfig) var config
    
    var body: some View {
        if config.isTablet {
            // 平板布局
        } else {
            // 手机布局
        }
    }
}
```

### 4.4 断点定义

```kotlin
object Breakpoints {
    val phone: Dp = 600.dp      // 手机
    val tablet: Dp = 840.dp     // 平板
    val desktop: Dp = 1200.dp   // 桌面
}
```

## 5. 依赖注入框架

### 5.1 AppContainer - 依赖容器

```kotlin
// 获取容器
val container = AppContainerHolder.getContainer()

// 使用服务
val photoService = container.photoService
val configService = container.configService

// 清理资源
AppContainerHolder.dispose()
```

### 5.2 特性

- ✅ 统一管理所有依赖
- ✅ 懒加载初始化
- ✅ 生命周期管理
- ✅ 易于测试和替换

## 6. 日志框架

### 6.1 使用方式

```kotlin
// 基础用法
Log.d("Tag", "Debug message")
Log.i("Tag", "Info message")
Log.e("Tag", "Error message", exception)

// 便捷方法
class MyClass {
    fun doSomething() {
        logD("Doing something")
        logE("Error occurred", exception)
    }
}
```

### 6.2 特性

- ✅ 统一入口 `Log`（底层为 [Napier](https://github.com/AAkira/Napier) + 自定义 `Antilog`）
- ✅ 平台控制台（Android Logcat / iOS NSLog）
- ✅ 支持异常堆栈随条目录入远程队列（经 [LogSanitizer](shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/logger/LogSanitizer.kt) 脱敏）
- ✅ 本地 NDJSON 文件队列 + [RemoteLogUploadScheduler](shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/logger/RemoteLogUploadScheduler.kt) 定时/手动批量上传（Ktor）
- ✅ 类型安全的便捷方法（`logD` / `logE` 等）

初始化：在应用启动早期调用 `DiagnosticLogging.install(context)`，并由 `AppContainer.startDiagnosticLogUpload()` 启动上传调度（详见 `README_API.md` 诊断日志一节）。

## 7. 图片加载框架

### 7.1 使用方式

```kotlin
val imageLoader = SimpleImageLoader(httpClient)

// 直接加载
val result = imageLoader.loadImage(url)
when (result) {
    is ImageLoadResult.Success -> {
        // 使用图片数据
    }
    is ImageLoadResult.Error -> {
        // 处理错误
    }
    ImageLoadResult.Loading -> {
        // 显示加载中
    }
}

// 带缓存加载
val cachedResult = imageLoader.loadImageWithCache(url)

// 清理缓存
imageLoader.clearCache()
```

### 7.2 特性

- ✅ 内存缓存
- ✅ 自动缓存管理
- ✅ 支持大文件
- ✅ 可扩展（可替换为 Coil、Glide 等）

## 8. 错误处理框架

### 8.1 使用方式

```kotlin
try {
    // 可能出错的操作
} catch (e: Exception) {
    val appError = ErrorHandler.handleError(e)
    val userMessage = ErrorHandler.getUserMessage(appError)
    // 显示给用户
}
```

### 8.2 错误类型

- `NetworkError`: 网络相关错误
- `StorageError`: 存储相关错误
- `ValidationError`: 验证错误
- `UnknownError`: 未知错误

## 9. 配置管理框架

### 9.1 使用方式

```kotlin
val configManager = ConfigManager()
val config = configManager.getConfig()

if (configManager.isDebug()) {
    // 调试模式逻辑
}

if (config.enableLogging) {
    // 启用日志
}
```

### 9.2 环境配置

- `DEVELOPMENT`: 开发环境
- `STAGING`: 测试环境
- `PRODUCTION`: 生产环境

## 10. 权限管理框架

### 10.1 使用方式

```kotlin
val permissionManager = PermissionManager()

// 检查权限
val status = permissionManager.checkPermission(Permission.CAMERA)
when (status) {
    PermissionStatus.GRANTED -> {
        // 已授权
    }
    PermissionStatus.DENIED -> {
        // 请求权限
        permissionManager.requestPermission(Permission.CAMERA)
    }
    PermissionStatus.PERMANENTLY_DENIED -> {
        // 永久拒绝，引导用户到设置
    }
}
```

### 10.2 支持的权限

- `CAMERA`: 相机权限
- `PHOTO_LIBRARY`: 相册权限
- `STORAGE`: 存储权限

## 11. 工具类库

### 11.1 扩展函数

```kotlin
// 字符串
val str: String? = null
if (str.isNotNullOrBlank()) {
    // 非空且非空白
}

// 文件大小格式化
val size = 1024 * 1024 * 5L
val formatted = size.formatFileSize() // "5.00 MB"

// 安全转换
val value = nullableValue.orElse("default")
```

### 11.2 Toast 提示（Android）

```kotlin
@Composable
fun MyScreen() {
    val showToast = rememberToast()
    
    Button(onClick = {
        showToast("操作成功", ToastType.SUCCESS)
    }) {
        Text("点击")
    }
}
```

### 11.3 Toast 提示（iOS）

```swift
struct MyView: View {
    @State private var toastMessage: String?
    
    var body: some View {
        Button("点击") {
            toastMessage = "操作成功"
        }
        .toast(message: $toastMessage, type: .success)
    }
}
```

## 12. 最佳实践

1. **网络请求**：始终使用 `ApiResult` 封装结果，统一错误处理
2. **路由**：使用 `Screen` sealed class 定义路由，类型安全
3. **主题**：通过 `ThemeRepository` 持久化主题设置
4. **响应式**：使用 `ResponsiveContainer` 包裹需要适配的页面
5. **设备检测**：使用 `DeviceInfo` 或 `ResponsiveConfig` 进行设备特定逻辑
6. **依赖注入**：使用 `AppContainer` 统一管理依赖
7. **日志**：使用 `Log` 工具类记录日志，便于调试和问题追踪
8. **错误处理**：使用 `ErrorHandler` 统一处理错误，提供用户友好的提示
9. **权限管理**：使用 `PermissionManager` 统一处理权限请求

## 13. 扩展指南

### 添加新的网络拦截器

```kotlin
val httpClient = HttpClient {
    // 添加自定义拦截器
    engine {
        // 配置引擎
    }
}
```

### 添加新的路由

```kotlin
// Android
sealed class Screen {
    object NewScreen : Screen("new_screen")
}

// iOS
enum class AppRoute {
    case newScreen = "new_screen"
}
```

### 自定义主题颜色

```kotlin
val customColors = AppColors(
    primary = 0xFFYourColor,
    // ...
)
```

### 添加新的权限类型

```kotlin
enum class Permission {
    CAMERA,
    PHOTO_LIBRARY,
    STORAGE,
    NEW_PERMISSION  // 新增权限
}
```
