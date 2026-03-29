# 后端 API 接入说明

## 概述

移动端已成功接入后端 API，所有数据操作现在都通过后端 API 进行。

## 服务类说明

### 1. AuthService（认证服务）
- **位置**: `com.xichen.cloudphoto.service.AuthService`
- **构造**: 接收 `HttpClient`（由 [AppContainer](../core/di/AppContainer.kt) 提供**无默认 Token** 的实例，避免登录/注册误带旧 Token）；`PhotoApiService` / `AlbumApiService` / `ConfigApiService` 共用**带 Token** 的另一实例。
- **功能**: 用户注册、登录、刷新 Token、登出、修改密码等
- **已接入**: ✅ 已完成

### 2. PhotoApiService（照片 API 服务）
- **位置**: `com.xichen.cloudphoto.service.PhotoApiService`
- **功能**: 
  - 获取照片列表（支持分页、按相册筛选）
  - 获取照片详情
  - **上传照片（推荐）**：POST /api/photos/upload，由后端从数据库读取用户存储配置并经 SDK 转发到存储商，App 不直连存储商
  - 请求 Presigned URL（可选，仅需直传时使用）
  - 上传完成通知（直传完成后落库）
  - 删除照片
  - 批量删除照片
- **已接入**: ✅ 已完成

### 3. AlbumApiService（相册 API 服务）
- **位置**: `com.xichen.cloudphoto.service.AlbumApiService`
- **功能**:
  - 获取相册列表（支持分页）
  - 获取相册详情
  - 创建相册
  - 更新相册
  - 删除相册
  - 添加照片到相册
  - 从相册移除照片
- **已接入**: ✅ 已完成

### 4. ConfigApiService（对象存储配置 API 服务）
- **位置**: `com.xichen.cloudphoto.service.ConfigApiService`
- **功能**:
  - 获取配置列表
  - 获取配置详情
  - 保存配置（创建或更新）
  - 激活配置（设为默认）
  - 删除配置
  - 测试配置
- **已接入**: ✅ 已完成

## 使用方式

### 1. 获取服务实例

```kotlin
val container = AppContainerHolder.getContainer(context)
val photoApiService = container.photoApiService
val albumApiService = container.albumApiService
val configApiService = container.configApiService
val authService = container.authService
```

### 2. 调用 API 示例

#### 获取照片列表
```kotlin
val result = photoApiService.getPhotos(page = 1, size = 20)
result.onSuccess { pageDTO ->
    val photos = pageDTO.records.map { it.toPhoto() }
    // 使用照片列表
}.onError { exception, message ->
    // 处理错误
}
```

#### 创建相册
```kotlin
val result = albumApiService.createAlbum("我的相册")
result.onSuccess { albumDTO ->
    val album = albumDTO.toAlbum()
    // 使用相册
}.onError { exception, message ->
    // 处理错误
}
```

#### 保存对象存储配置
```kotlin
val configDTO = storageConfig.toCloudConfigDTO()
val result = configApiService.saveConfig(configDTO)
result.onSuccess { savedConfig ->
    // 配置保存成功
}.onError { exception, message ->
    // 处理错误
}
```

#### 推荐：后端代理上传（无需直连存储商）
```kotlin
// 将文件通过后端接口上传，后端从数据库读取用户存储配置，经各存储商 SDK 转发
val result = photoApiService.uploadPhoto(
    file = imageFile,  // MultipartFile / 本地文件
    albumId = null,
    takenAt = null
)
result.onSuccess { photoDTO ->
    // 上传成功，photoDTO 含完整 URL 等
}.onError { _, message -> }
```

#### 可选：前端直传流程（Presigned URL）
```kotlin
// 1. 请求 Presigned URL
val presignResult = photoApiService.requestPresignUrl(
    filename = "photo.jpg",
    contentType = "image/jpeg",
    size = photoData.size.toLong()
)

presignResult.onSuccess { presignResponse ->
    // 2. 直接上传到对象存储
    val uploadResult = uploadToObjectStorage(
        url = presignResponse.url,
        method = presignResponse.method ?: "PUT",
        data = photoData,
        headers = presignResponse.headers
    )
    
    if (uploadResult.isSuccess) {
        // 3. 通知后端上传完成
        photoApiService.completeUpload(
            remotePath = presignResponse.remotePath,
            size = photoData.size.toLong()
        )
    }
}
```

## Token 管理

### 自动添加 Token
所有 API 服务都配置了 Token 拦截器，会自动在请求头中添加 `Authorization: Bearer <token>`。

### Token 存储
Token 通过 `TokenManager` 管理，存储在平台特定的存储中：
- Android: SharedPreferences
- iOS: UserDefaults

### 登录后保存 Token
```kotlin
val loginResult = authService.login(LoginRequest(account, password))
loginResult.onSuccess { loginResponse ->
    // 保存 Token
    tokenManager.saveAccessToken(loginResponse.accessToken)
    tokenManager.saveRefreshToken(loginResponse.refreshToken)
}
```

## 模型转换

### DTO 转本地模型
- `PhotoDTO.toPhoto()`: 转换为本地 Photo 模型
- `AlbumDTO.toAlbum()`: 转换为本地 Album 模型
- `CloudConfigDTO.toStorageConfig()`: 转换为本地 StorageConfig 模型

### 本地模型转 DTO
- `StorageConfig.toCloudConfigDTO()`: 转换为 CloudConfigDTO

## 配置

### API 基础 URL
在 `ApiConfig.AUTH_BASE_URL` 中配置：
```kotlin
object ApiConfig {
    const val AUTH_BASE_URL: String = "http://127.0.0.1:8080"
}
```

### 开发环境配置
- 使用 ADB 端口转发：`adb reverse tcp:8080 tcp:8080`
- 或直接使用服务器 IP 地址

### 客户端诊断日志（可上传）
移动端通过 **Napier → 自定义 Antilog** 统一输出到控制台，并将脱敏后的结构化行写入本地 NDJSON 队列（Android：`cacheDir/cloudphoto_remote_logs.ndjson`；iOS：Caches 目录下同文件名）。  
`AppContainer.startDiagnosticLogUpload()` 会注册带 Token 的 `HttpClient`；**定时上传**由 `RemoteLogConfig.periodicRemoteUploadEnabled` 控制（默认 `false`，避免静默耗流量）。用户可在应用内 **关于 → 上传日志** 调用 `RemoteLogUploadScheduler.uploadDiagnosticLogsNow()` 主动清空队列。若将 `periodicRemoteUploadEnabled` 设为 `true`，则恢复约 **每 5 分钟** 批量 POST；亦可 `RemoteLogUploadScheduler.requestFlushNow()` 异步触发单批上传。

- **路径常量**：`ApiConfig.CLIENT_LOG_BATCH_PATH`（默认 `"/api/client-logs/batch"`，与 `AUTH_BASE_URL` 拼接）
- **HTTP**：`POST`，`Content-Type: application/json`，与业务 API 共用带 Token 的 `HttpClient`（未登录时不带 `Authorization`）
- **请求体** `ClientLogBatchRequest`：
  - `batchId: String` — 本次批次 ID（十六进制随机串）
  - `platform: String` — `"android"` / `"ios"`
  - `osVersion: String`、`appVersion: String`、`deviceModel: String?`
  - `entries: List<ClientLogEntry>`，每项含 `tsEpochMillis`、`level`（与 `LogLevel` 同名）、`tag`、`message`、`stack`（可选，截断后的堆栈）
- **响应**：建议 `200`/`204` 空 body；客户端不解析 body，仅判断 HTTP 成功即删除本批已上传行。
- **服务端建议**：校验体大小与条数上限；按 `batchId` 去重；落库（MySQL / MongoDB / ClickHouse 等）后提供按用户、时间、tag 检索的管理后台。未登录用户也可写入匿名设备维度，需注意隐私与留存策略。
- **客户端开关**：`RemoteLogConfig.bufferToFileEnabled` 为 `false` 时仅控制台日志，不再写入上传队列；`RemoteLogConfig.periodicRemoteUploadEnabled` 控制是否后台定时上传。

### 用户云端推送开关（消息通知）

客户端 **「我的 → 消息通知」** 仅控制服务端是否向该用户账号**下发推送**（业务策略），**不是**系统通知权限。

- **路径常量**：`ApiConfig.USER_PUSH_PREFERENCE_PATH`（默认 `"/api/user/push-preference"`，与 `AUTH_BASE_URL` 拼接）
- **鉴权**：需登录，`Authorization: Bearer <accessToken>`
- **GET**：响应体为统一封装 `ApiResponse<UserPushPreferenceDto>`，`data` 形如 `{ "pushEnabled": true }`
- **PUT**：请求体 `{ "pushEnabled": true | false }`（关闭时字段仍需下发 `false`，客户端已用 `@EncodeDefault(ALWAYS)` 保证序列化）
- **服务端建议**：按用户 ID 持久化；实际下发 FCM/APNs 前检查 `pushEnabled`；新用户默认可为 `true`

## 注意事项

1. **Token 过期处理**: 当 Token 过期时，需要调用 `authService.refreshToken()` 刷新 Token
2. **错误处理**: 所有 API 调用都返回 `ApiResult<T>`，需要正确处理成功和错误情况
3. **资源清理**: 服务类提供了 `close()` 方法，在不需要时应该关闭以释放资源
4. **分页**: 照片和相册列表都支持分页，注意处理分页逻辑

## 迁移指南

### 从本地服务迁移到 API 服务

**之前（本地服务）**:
```kotlin
val photos = photoService.getAllPhotos()
```

**现在（API 服务）**:
```kotlin
val result = photoApiService.getPhotos(page = 1, size = 20)
result.onSuccess { pageDTO ->
    val photos = pageDTO.records.map { it.toPhoto() }
}
```

## 后续优化

1. **缓存策略**: 可以考虑添加本地缓存，减少 API 调用
2. **离线支持**: 可以保留本地服务作为离线备用方案
3. **同步机制**: 实现数据同步，确保本地和服务器数据一致
