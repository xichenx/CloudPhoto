package com.xichen.cloudphoto.core.network

import com.xichen.cloudphoto.core.logger.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 网络客户端配置
 */
object NetworkConfig {
    const val DEFAULT_TIMEOUT = 30_000L
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
        enableLogging: Boolean = true
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
            
            // 日志
            if (enableLogging) {
                install(Logging) {
                    level = LogLevel.ALL  // 改为ALL级别，输出更详细的日志
                    logger = object : Logger {
                        override fun log(message: String) {
                            // 可以在这里添加自定义日志逻辑
                            Log.d("NetWorkClient","Network: $message")
                        }
                    }
                }
            }
            
            // 默认请求头
            defaultRequest {
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                    append(HttpHeaders.Accept, ContentType.Application.Json)
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

/**
 * 网络请求扩展函数
 * 使用 Ktor 的原始 request 方法避免递归调用
 */
suspend inline fun <reified T> HttpClient.get(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = request(urlString) {
            method = HttpMethod.Get
            block()
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

suspend inline fun <reified T> HttpClient.post(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = request(urlString) {
            method = HttpMethod.Post
            block()
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

suspend inline fun <reified T> HttpClient.put(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = request(urlString) {
            method = HttpMethod.Put
            block()
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

suspend inline fun <reified T> HttpClient.delete(
    urlString: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = request(urlString) {
            method = HttpMethod.Delete
            block()
        }
        ApiResult.Success(response.body())
    } catch (e: Exception) {
        ApiResult.Error(e, e.message)
    }
}

