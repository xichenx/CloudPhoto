# 网络请求框架使用指南

本文档介绍如何使用 CloudPhoto 项目中的网络请求框架。

## 目录

1. [框架概述](#框架概述)
2. [快速开始](#快速开始)
3. [创建 HttpClient](#创建-httpclient)
4. [发送请求](#发送请求)
5. [处理响应](#处理响应)
6. [完整示例](#完整示例)
7. [注意事项](#注意事项)

## 框架概述

本框架基于 Ktor 构建，提供了以下特性：

- ✅ 统一的 `ApiResult` 响应封装
- ✅ 自动 JSON 序列化/反序列化
- ✅ 请求超时配置
- ✅ 日志记录
- ✅ 异常处理
- ✅ 支持 GET、POST、PUT、DELETE 方法

## 快速开始

### 1. 创建 HttpClient

```kotlin
import com.xichen.cloudphoto.core.network.NetworkClientFactory
import io.ktor.client.HttpClient

// 创建带基础URL的客户端
val httpClient = NetworkClientFactory.create(
    baseUrl = "https://api.example.com",
    timeout = 30_000L,
    enableLogging = true
)

// 或创建不带基础URL的客户端（适用于访问多个不同域名）
val httpClient = NetworkClientFactory.create(
    baseUrl = null,
    timeout = 60_000L
)
```

### 2. 定义数据模型

使用 `@Serializable` 注解标记数据类：

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String
)
```

### 3. 发送请求

```kotlin
import com.xichen.cloudphoto.core.network.get
import com.xichen.cloudphoto.core.network.post

// GET 请求
val result = httpClient.get<User>("/api/users/123")

// POST 请求
val request = CreateUserRequest(name = "张三", email = "zhangsan@example.com")
val result = httpClient.post<User>("/api/users") {
    setBody(request)
}
```

### 4. 处理响应

```kotlin
when (result) {
    is ApiResult.Success -> {
        val user = result.data
        println("成功: ${user.name}")
    }
    is ApiResult.Error -> {
        println("失败: ${result.message}")
    }
    is ApiResult.Loading -> {
        println("加载中...")
    }
}
```

## 创建 HttpClient

### 使用 NetworkClientFactory

`NetworkClientFactory` 提供了便捷的工厂方法创建预配置的 `HttpClient`：

```kotlin
val httpClient = NetworkClientFactory.create(
    baseUrl = "https://api.example.com",  // 基础URL（可选）
    timeout = 30_000L,                     // 请求超时时间（毫秒，默认30秒）
    enableLogging = true                  // 是否启用日志（默认true）
)
```

### 配置说明

- **baseUrl**: 设置后，所有相对路径的请求都会基于此URL
- **timeout**: 请求超时时间，默认30秒
- **enableLogging**: 是否启用请求日志，默认启用

## 发送请求

框架提供了四个扩展函数：`get`、`post`、`put`、`delete`，它们都返回 `ApiResult<T>`。

### GET 请求

```kotlin
// 简单GET请求
val result = httpClient.get<User>("/api/users/123")

// 带查询参数
val result = httpClient.get<List<User>>("/api/users/search") {
    parameter("keyword", "张三")
    parameter("page", 1)
    parameter("size", 20)
}

// 带自定义请求头
val result = httpClient.get<User>("/api/users/123") {
    headers {
        append(HttpHeaders.Authorization, "Bearer $token")
    }
}
```

### POST 请求

```kotlin
// 发送JSON数据
val request = CreateUserRequest(name = "张三", email = "zhangsan@example.com")
val result = httpClient.post<User>("/api/users") {
    setBody(request)
}

// 带请求头和参数
val result = httpClient.post<User>("/api/users") {
    headers {
        append(HttpHeaders.Authorization, "Bearer $token")
    }
    setBody(request)
}
```

### PUT 请求

```kotlin
val user = User(id = "123", name = "李四", email = "lisi@example.com")
val result = httpClient.put<User>("/api/users/123") {
    setBody(user)
}
```

### DELETE 请求

```kotlin
val result = httpClient.delete<Unit>("/api/users/123")
```

### 使用完整URL

即使设置了 `baseUrl`，也可以使用完整URL：

```kotlin
val result = httpClient.get<User>("https://other-api.com/users/123")
```

## 处理响应

### ApiResult 类型

`ApiResult` 是一个密封类，有三种状态：

- `ApiResult.Success<T>`: 请求成功，包含数据
- `ApiResult.Error`: 请求失败，包含异常和错误信息
- `ApiResult.Loading`: 请求进行中（通常用于UI状态）

### 方式一：使用 when 表达式

```kotlin
when (result) {
    is ApiResult.Success -> {
        val user = result.data
        // 处理成功情况
    }
    is ApiResult.Error -> {
        val exception = result.exception
        val message = result.message
        // 处理错误情况
    }
    is ApiResult.Loading -> {
        // 处理加载状态
    }
}
```

### 方式二：使用扩展函数

```kotlin
result
    .onSuccess { user ->
        // 处理成功情况
        println("用户: ${user.name}")
    }
    .onError { exception, message ->
        // 处理错误情况
        println("错误: $message")
    }
    .onLoading {
        // 处理加载状态
        println("加载中...")
    }
```

### 方式三：直接检查类型

```kotlin
if (result is ApiResult.Success) {
    val user = result.data
    // 处理数据
} else if (result is ApiResult.Error) {
    // 处理错误
    println("错误: ${result.message}")
}
```

## 完整示例

### Service 类示例

```kotlin
import com.xichen.cloudphoto.core.network.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*

class UserService(private val httpClient: HttpClient) {
    
    suspend fun getUser(userId: String): ApiResult<User> {
        return httpClient.get<User>("/api/users/$userId")
    }
    
    suspend fun createUser(name: String, email: String): ApiResult<User> {
        val request = CreateUserRequest(name = name, email = email)
        return httpClient.post<User>("/api/users") {
            setBody(request)
        }
    }
    
    suspend fun updateUser(userId: String, name: String, email: String): ApiResult<User> {
        val user = User(id = userId, name = name, email = email)
        return httpClient.put<User>("/api/users/$userId") {
            setBody(user)
        }
    }
    
    suspend fun deleteUser(userId: String): ApiResult<Unit> {
        return httpClient.delete<Unit>("/api/users/$userId")
    }
    
    suspend fun searchUsers(keyword: String): ApiResult<List<User>> {
        return httpClient.get<List<User>>("/api/users/search") {
            parameter("keyword", keyword)
        }
    }
}
```

### ViewModel 中使用

```kotlin
import com.xichen.cloudphoto.core.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel {
    private val httpClient = NetworkClientFactory.create(
        baseUrl = "https://api.example.com"
    )
    private val userService = UserService(httpClient)
    
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState
    
    suspend fun loadUser(userId: String) {
        _uiState.value = UserUiState.Loading
        
        val result = userService.getUser(userId)
        
        when (result) {
            is ApiResult.Success -> {
                _uiState.value = UserUiState.Success(result.data)
            }
            is ApiResult.Error -> {
                _uiState.value = UserUiState.Error(result.message ?: "未知错误")
            }
            is ApiResult.Loading -> {
                _uiState.value = UserUiState.Loading
            }
        }
    }
}

sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: User) : UserUiState()
    data class Error(val message: String) : UserUiState()
}
```

## 注意事项

### 1. 数据模型必须可序列化

所有用于请求体和响应体的数据类必须使用 `@Serializable` 注解：

```kotlin
@Serializable
data class User(...)
```

### 2. 在协程中调用

所有网络请求函数都是 `suspend` 函数，必须在协程中调用：

```kotlin
// ✅ 正确
viewModelScope.launch {
    val result = httpClient.get<User>("/api/users/123")
}

// ❌ 错误 - 不能在非协程作用域中直接调用
val result = httpClient.get<User>("/api/users/123")
```

### 3. 异常处理

框架会自动捕获异常并返回 `ApiResult.Error`，但建议在关键位置添加额外的错误处理：

```kotlin
val result = httpClient.get<User>("/api/users/123")
result.onError { exception, message ->
    when (exception) {
        is HttpRequestTimeoutException -> {
            // 处理超时
        }
        is ClientRequestException -> {
            // 处理客户端错误（4xx）
        }
        is ServerResponseException -> {
            // 处理服务器错误（5xx）
        }
        else -> {
            // 处理其他错误
        }
    }
}
```

### 4. 请求头设置

默认情况下，框架会自动设置 `Content-Type: application/json` 和 `Accept: application/json`。如果需要修改，可以在请求的 `block` 中覆盖：

```kotlin
val result = httpClient.post<String>("/api/upload") {
    headers {
        remove(HttpHeaders.ContentType)
        append(HttpHeaders.ContentType, ContentType.MultiPart.FormData)
    }
}
```

### 5. 资源清理

如果创建了自定义的 `HttpClient`，记得在使用完毕后关闭：

```kotlin
val httpClient = NetworkClientFactory.create(...)
try {
    // 使用 httpClient
} finally {
    httpClient.close()
}
```

如果通过依赖注入容器（如 `AppContainer`）获取 `HttpClient`，容器会自动管理生命周期。

## 更多示例

更多使用示例请参考 `NetworkUsageExample.kt` 文件。


