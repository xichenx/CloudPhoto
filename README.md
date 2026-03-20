# CloudPhoto - 云相册应用

基于 **Kotlin Multiplatform (KMP)** 的云相册应用，支持 Android（Jetpack Compose）与 iOS（SwiftUI）。数据通过后端 API 管理。**图片上传统一由 App 调用后端接口**：App 将文件提交到后端，后端从数据库读取用户已配置的存储信息，通过各存储商官方 SDK 转发到存储商，App 无需直连存储商 API。

## 功能特性

- **账号与认证**：注册、登录、Token 刷新、修改密码、登出
- **拍照即上传**：调用系统相机拍照后，通过后端 **POST /api/photos/upload** 接口上传（后端从数据库读取用户存储配置并转发到存储商），无需直连存储商
- **多存储支持**：对象存储配置由用户在前端保存到数据库，后端从数据库获取配置，通过各存储商 **官方 SDK** 接入
  - 阿里云 OSS
  - AWS S3
  - 腾讯云 COS
  - MinIO
  - 七牛云
  - 自定义 S3 兼容存储
- **跨平台**：KMP 共享业务逻辑与网络层，Android / iOS 各自实现 UI（不共享 UI）
- **五个主 Tab**：
  - **照片**：查看云端照片列表（分页、按相册筛选）
  - **相册**：相册列表、创建/编辑/删除相册、管理相册内照片
  - **拍照**：调起相机拍照并上传
  - **空间**：对象存储配置列表、添加/编辑/删除配置、设为默认、测试连接
  - **我的**：个人资料、账户安全、修改密码、主题设置、退出登录等

## 架构概览

- **移动端**：本仓库 KMP 项目（`shared` + `composeApp` + `iosApp`）
- **后端**：需单独部署 [CloudPhotoAPI](../CloudPhotoAPI) 服务（如 `http://host:8080`），提供认证、照片、相册、存储配置等 REST API
- **数据流**：登录后请求携带 Token → 照片/相册/配置均通过后端 API 读写 → 上传照片时 App 调用 **POST /api/photos/upload**，后端从数据库取用户存储配置，经 SDK 转发到存储商并落库

## 项目结构

```
CloudPhoto/
├── shared/                          # KMP 共享模块
│   └── src/
│       ├── commonMain/kotlin/.../
│       │   ├── model/               # 数据模型（Photo, Album, StorageConfig, DTO 等）
│       │   ├── storage/             # 对象存储接口与各厂商实现（OSS/S3/COS/MinIO/Qiniu/CustomS3）
│       │   ├── repository/          # 数据仓库（PhotoRepository, AlbumRepository, ConfigRepository）
│       │   ├── service/             # 业务与 API 服务（Auth, Photo, Album, Config API 及本地 ConfigService）
│       │   ├── core/                # 公共能力
│       │   │   ├── network/        # Ktor 封装、ApiResult、NetworkClientFactory
│       │   │   ├── auth/           # Token 管理（expect/actual）
│       │   │   ├── config/         # ApiConfig、AppConfig、ConfigManager
│       │   │   ├── permission/     # 权限（expect/actual）
│       │   │   ├── theme/          # 主题（ThemeRepository）
│       │   │   ├── logger/         # 日志（expect/actual）
│       │   │   ├── image/         # 图片加载接口
│       │   │   ├── error/         # 错误处理
│       │   │   └── di/            # AppContainer 依赖组装
│       │   └── util/               # 工具（TimeUtils, Base64Encoder 等）
│       ├── androidMain/             # Android 平台实现（Repository、TokenManager、ConfigManager 等）
│       └── iosMain/                # iOS 平台实现
├── composeApp/                      # Android 应用（Compose Multiplatform + Android 目标）
│   └── src/androidMain/kotlin/.../
│       ├── ui/                     # 各功能界面（Photos, Albums, Camera, Storage, Settings 等）
│       ├── navigation/             # NavGraph、AuthNavGraph、Screen 路由
│       ├── theme/                  # 主题与 Material3
│       ├── App.kt, MainActivity.kt
│       └── AppViewModel.kt
└── iosApp/                          # iOS 应用（SwiftUI）
    └── iosApp/
        ├── ContentView.swift       # 根视图（未登录：登录/注册；已登录：TabView）
        ├── AppViewModel.swift
        ├── Views/                  # 照片、相册、拍照、空间、设置等视图
        ├── Navigation/             # NavigationCoordinator
        └── Theme/                  # AppTheme
```

## 技术栈

### 共享模块 (KMP)

- Kotlin 2.2.x、Kotlin Multiplatform
- Ktor 2.3.x（Core、ContentNegotiation、Logging；Android 用 `ktor-client-android`，iOS 用 `ktor-client-darwin`）
- Kotlinx Serialization JSON、Kotlinx Coroutines、Kotlinx DateTime

### Android

- Jetpack Compose（Compose Multiplatform）、Material3
- ViewModel、Lifecycle、Navigation Compose
- Activity Result API（相机/权限）
- Accompanist System UI Controller、Material Icons Extended

### iOS

- SwiftUI、Combine
- 通过 KMP 生成的 `Shared` framework 调用共享逻辑
- UIImagePickerController / PHPicker 等系统 API 用于相机与相册

## 环境要求

- **JDK 11+**（Android / 共享模块编译）
- **Android Studio** 或 **IntelliJ IDEA**（开发 Android / shared）
- **Xcode**（开发 iOS，打开 `iosApp/iosApp.xcodeproj`）
- **后端服务**：需先启动 [CloudPhotoAPI](../CloudPhotoAPI)（默认假定 `http://127.0.0.1:8080`，可在 `shared` 的 `ApiConfig.AUTH_BASE_URL` 中修改）

## 使用说明

### 1. 启动后端

确保 CloudPhotoAPI 已运行并可访问（如 `http://127.0.0.1:8080`）。  
真机调试 Android 时若本机跑后端，可使用：

```bash
adb reverse tcp:8080 tcp:8080
```

这样应用内仍可使用 `http://127.0.0.1:8080`。

### 2. 注册与登录

打开应用 → 注册账号或登录 → 登录成功后进入主界面（底部五个 Tab）。

### 3. 对象存储配置（空间 Tab）

- 进入「空间」Tab，点击添加
- 填写配置名称、提供商、Endpoint、Access Key、Secret、Bucket、Region 等
- 可设为默认、测试连接、编辑或删除

（实际存储的创建与配置由后端与 CloudPhotoAPI 管理，此处为应用内配置的展示与编辑。）

### 4. 拍照上传

- 进入「拍照」Tab，点击拍照
- 授予相机权限后拍摄，确认后通过后端 **/api/photos/upload** 上传（后端转发到用户配置的存储商，App 不直连存储商）

### 5. 照片与相册

- **照片** Tab：查看云端照片列表，支持分页与按相册筛选
- **相册** Tab：创建/编辑/删除相册，管理相册内照片

### 6. 我的

个人资料、账户安全、修改密码、主题设置、帮助与反馈、关于、退出登录等。

## 开发说明

### 构建

**Android：**

```bash
./gradlew :composeApp:assembleDebug
```

**iOS：**

在 Xcode 中打开 `iosApp/iosApp.xcodeproj`，选择目标设备/模拟器后构建运行。

### API 与配置

- 接口基地址：`shared/src/commonMain/.../core/config/AppConfig.kt` 中 `ApiConfig.AUTH_BASE_URL`（默认 `http://127.0.0.1:8080`）
- 后端 API 使用方式、**上传流程（推荐走后端 /api/photos/upload）**、Token 管理、模型转换等见：  
  **`shared/src/commonMain/kotlin/com/xichen/cloudphoto/service/README_API.md`**
- 网络层（Ktor、ApiResult、请求封装）见：  
  **`shared/src/commonMain/.../core/network/README.md`**

### 添加新的对象存储提供商

1. 在 `shared/.../model/StorageConfig.kt` 的 `StorageProvider` 枚举中增加新类型
2. 后端在 `CloudStorageServiceImpl` 中按提供商分支，使用该厂商 **官方 SDK** 实现上传/删除/获取 URL（配置从数据库读取）
3. 客户端在配置项中增加展示与枚举即可，上传统一走后端接口

### 注意事项

- 生产环境请使用 HTTPS 并正确配置后端地址；勿在代码中提交真实密钥
- 存储商密钥由用户在前端配置并加密存库，后端从数据库读取后经 SDK 访问存储商，客户端不直连存储商 API
- 相册、照片元数据（宽高、大小等）以后端与 API 设计为准，客户端按 DTO 展示

## 文档索引

| 文档 | 说明 |
|------|------|
| [README_API.md](shared/src/commonMain/kotlin/com/xichen/cloudphoto/service/README_API.md) | 后端 API 接入、上传接口（推荐 /upload）、Token、模型转换 |
| [core/network/README.md](shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/network/README.md) | 网络请求框架（Ktor、ApiResult）使用说明 |
| [core/permission/README.md](shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/permission/README.md) | 权限（expect/actual）说明（若有） |

## 许可证

MIT License
