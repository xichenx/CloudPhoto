package com.xichen.cloudphoto.core.error

import com.xichen.cloudphoto.core.logger.Log
import com.xichen.cloudphoto.core.network.HttpResponseException

/**
 * 应用错误类型
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class StorageError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class ValidationError(
        override val message: String
    ) : AppError(message)
    
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * 错误处理器
 */
object ErrorHandler {
    /**
     * 处理错误并返回用户友好的消息
     */
    fun handleError(error: Throwable): AppError {
        Log.e("ErrorHandler", "Error occurred", error)
        
        return when {
            error is HttpResponseException -> {
                when (error.status.value) {
                    401, 403 -> AppError.NetworkError("登录已失效，请重新登录", error)
                    404 -> AppError.NetworkError("请求的资源不存在", error)
                    in 500..599 -> AppError.NetworkError("服务暂时不可用，请稍后重试", error)
                    else -> AppError.NetworkError(error.message, error)
                }
            }
            error.message?.contains("network", ignoreCase = true) == true ||
            error.message?.contains("connection", ignoreCase = true) == true ||
            error.message?.contains("timeout", ignoreCase = true) == true ||
            error.message?.contains("host", ignoreCase = true) == true -> {
                AppError.NetworkError("网络连接失败，请检查网络设置", error)
            }
            error.message?.contains("storage", ignoreCase = true) == true ||
            error.message?.contains("bucket", ignoreCase = true) == true ||
            error.message?.contains("upload", ignoreCase = true) == true -> {
                AppError.StorageError("存储操作失败", error)
            }
            error is IllegalArgumentException -> {
                AppError.ValidationError(error.message ?: "参数错误")
            }
            else -> {
                AppError.UnknownError(error.message ?: "未知错误", error)
            }
        }
    }
    
    /**
     * 获取用户友好的错误消息
     */
    fun getUserMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> error.message
            is AppError.StorageError -> error.message
            is AppError.ValidationError -> error.message
            is AppError.UnknownError -> "操作失败，请稍后重试"
        }
    }
}

