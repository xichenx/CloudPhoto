package com.xichen.cloudphoto.core.network

import io.ktor.http.HttpStatusCode

/**
 * HTTP 层非 2xx 响应（在解析业务 [ApiResponse] 之前即可识别）。
 */
class HttpResponseException(
    val status: HttpStatusCode,
    override val message: String
) : Exception(message)
