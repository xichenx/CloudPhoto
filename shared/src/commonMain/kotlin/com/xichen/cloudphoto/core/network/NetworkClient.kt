package com.xichen.cloudphoto.core.network

import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.auth.AuthEvents
import com.xichen.cloudphoto.core.auth.TokenRefresher
import com.xichen.cloudphoto.core.logger.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.PublishedApi

/**
 * 请求体模式（类比 Retrofit：默认 JSON，multipart 在接口处显式声明）。
 *
 * - [Json]：在发起请求前设置 `Content-Type: application/json`，便于 `setBody(@Serializable …)` 与 Android 上 ContentNegotiation 工作。
 * - [Multipart]：不预设 JSON；在 `block` 里 `setBody(MultiPartFormDataContent(...))`，由 Ktor 设置 `multipart/form-data` 与 boundary。
 */
enum class HttpBodyMode {
    Json,
    Multipart
}

/**
 * 网络客户端配置
 */
object NetworkConfig {
    const val DEFAULT_TIMEOUT = 30_000L
    const val API_TIMEOUT = 60_000L
    const val CONNECT_TIMEOUT = 10_000L
    const val SOCKET_TIMEOUT = 30_000L
}

/**
 * 网络客户端工厂
 */
object NetworkClientFactory {
    fun create(
        baseUrl: String? = null,
        timeout: Long = NetworkConfig.DEFAULT_TIMEOUT,
        enableLogging: Boolean = true,
        logLevel: LogLevel = LogLevel.INFO,
        tokenManager: com.xichen.cloudphoto.core.auth.TokenManager? = null
    ): HttpClient {
        return HttpClient {
            // 基础配置
            baseUrl?.let { 
                defaultRequest {
                    url(it)
                }
            }
            
            // 超时配置
            install(HttpTimeout) {
                requestTimeoutMillis = timeout
                connectTimeoutMillis = NetworkConfig.CONNECT_TIMEOUT
                socketTimeoutMillis = NetworkConfig.SOCKET_TIMEOUT
            }
            
            // JSON 序列化
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                })
            }
            
            // 日志：默认 INFO，避免 ALL 刷屏与敏感信息过量输出；调试可传入 LogLevel.ALL
            if (enableLogging) {
                install(Logging) {
                    level = logLevel
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("NetworkClient", message)
                        }
                    }
                }
            }

            // 默认请求头
            defaultRequest {
                headers {
                    // NOTE: Do NOT force Content-Type here.
                    // Forcing `application/json` breaks multipart uploads (must be multipart/form-data + boundary).
                    append(HttpHeaders.Accept, ContentType.Application.Json)
                    
                    // Token 拦截器（自动添加 Authorization header）
                    tokenManager?.let { manager ->
                        val token = manager.getAccessToken()
                        if (token != null && !contains(HttpHeaders.Authorization)) {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }
                }
            }
            
            // 异常处理 - 可以通过自定义拦截器实现
            // HttpRequestRetry 需要额外依赖，这里先注释掉
            // install(HttpRequestRetry) {
            //     retryOnServerErrors(maxRetries = 3)
            //     exponentialDelay()
            // }
        }
    }
}

@PublishedApi
internal suspend fun failureResultFromResponse(
    response: io.ktor.client.statement.HttpResponse
): ApiResult.Error {
    val bodySnippet = runCatching { response.bodyAsText() }.getOrNull()
    val message = bodySnippet?.trim()?.takeIf { it.isNotEmpty() }
        ?: "${response.status.value} ${response.status.description}"
    if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
        AuthEvents.unauthorized(message)
    }
    return ApiResult.Error(
        HttpResponseException(response.status, message),
        message
    )
}

@PublishedApi
internal fun isUnauthorized(status: HttpStatusCode): Boolean {
    return status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden
}

/**
 * 网络请求扩展函数：先校验 HTTP 状态，再反序列化 body，避免 4xx/5xx 时误解析为成功。
 */
suspend inline fun <reified T> HttpClient.get(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.Default) {
    try {
        var response = request(urlString) {
            method = HttpMethod.Get
            block()
        }
        if (!response.status.isSuccess() && isUnauthorized(response.status)) {
            val refreshed = TokenRefresher.tryRefresh()
            if (refreshed) {
                response = request(urlString) {
                    method = HttpMethod.Get
                    block()
                }
            }
        }
        if (!response.status.isSuccess()) {
            if (isUnauthorized(response.status)) {
                AuthEvents.unauthorized(runCatching { response.bodyAsText() }.getOrNull())
            }
            return@withContext failureResultFromResponse(response)
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

suspend inline fun <reified T> HttpClient.post(
    urlString: String,
    bodyMode: HttpBodyMode = HttpBodyMode.Json,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.Default) {
    try {
        var response = request(urlString) {
            method = HttpMethod.Post
            if (bodyMode == HttpBodyMode.Json) {
                contentType(ContentType.Application.Json)
            }
            block()
        }
        if (!response.status.isSuccess() && isUnauthorized(response.status)) {
            val refreshed = TokenRefresher.tryRefresh()
            if (refreshed) {
                response = request(urlString) {
                    method = HttpMethod.Post
                    if (bodyMode == HttpBodyMode.Json) {
                        contentType(ContentType.Application.Json)
                    }
                    block()
                }
            }
        }
        if (!response.status.isSuccess()) {
            if (isUnauthorized(response.status)) {
                AuthEvents.unauthorized(runCatching { response.bodyAsText() }.getOrNull())
            }
            return@withContext failureResultFromResponse(response)
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

/**
 * POST 请求且无需解析响应体（如 204、空 body），避免对 [Unit] 误用 [body] 反序列化。
 */
suspend inline fun HttpClient.postUnit(
    urlString: String,
    bodyMode: HttpBodyMode = HttpBodyMode.Json,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<Unit> = withContext(Dispatchers.Default) {
    try {
        var response = request(urlString) {
            method = HttpMethod.Post
            if (bodyMode == HttpBodyMode.Json) {
                contentType(ContentType.Application.Json)
            }
            block()
        }
        if (!response.status.isSuccess() && isUnauthorized(response.status)) {
            val refreshed = TokenRefresher.tryRefresh()
            if (refreshed) {
                response = request(urlString) {
                    method = HttpMethod.Post
                    if (bodyMode == HttpBodyMode.Json) {
                        contentType(ContentType.Application.Json)
                    }
                    block()
                }
            }
        }
        if (!response.status.isSuccess()) {
            if (isUnauthorized(response.status)) {
                AuthEvents.unauthorized(runCatching { response.bodyAsText() }.getOrNull())
            }
            return@withContext failureResultFromResponse(response)
        }
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

suspend inline fun <reified T> HttpClient.put(
    urlString: String,
    bodyMode: HttpBodyMode = HttpBodyMode.Json,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.Default) {
    try {
        var response = request(urlString) {
            method = HttpMethod.Put
            if (bodyMode == HttpBodyMode.Json) {
                contentType(ContentType.Application.Json)
            }
            block()
        }
        if (!response.status.isSuccess() && isUnauthorized(response.status)) {
            val refreshed = TokenRefresher.tryRefresh()
            if (refreshed) {
                response = request(urlString) {
                    method = HttpMethod.Put
                    if (bodyMode == HttpBodyMode.Json) {
                        contentType(ContentType.Application.Json)
                    }
                    block()
                }
            }
        }
        if (!response.status.isSuccess()) {
            if (isUnauthorized(response.status)) {
                AuthEvents.unauthorized(runCatching { response.bodyAsText() }.getOrNull())
            }
            return@withContext failureResultFromResponse(response)
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

/**
 * POST `multipart/form-data`（等价于 [post] + [HttpBodyMode.Multipart]），类比 Retrofit `@Multipart`。
 */
suspend inline fun <reified T> HttpClient.postMultipart(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = post(urlString, HttpBodyMode.Multipart, block)

/**
 * PUT `multipart/form-data`（等价于 [put] + [HttpBodyMode.Multipart]），需要时再用。
 */
suspend inline fun <reified T> HttpClient.putMultipart(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = put(urlString, HttpBodyMode.Multipart, block)

suspend inline fun <reified T> HttpClient.delete(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.Default) {
    try {
        var response = request(urlString) {
            method = HttpMethod.Delete
            block()
        }
        if (!response.status.isSuccess() && isUnauthorized(response.status)) {
            val refreshed = TokenRefresher.tryRefresh()
            if (refreshed) {
                response = request(urlString) {
                    method = HttpMethod.Delete
                    block()
                }
            }
        }
        if (!response.status.isSuccess()) {
            if (isUnauthorized(response.status)) {
                AuthEvents.unauthorized(runCatching { response.bodyAsText() }.getOrNull())
            }
            return@withContext failureResultFromResponse(response)
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

