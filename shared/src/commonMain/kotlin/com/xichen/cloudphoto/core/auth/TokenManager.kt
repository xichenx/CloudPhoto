package com.xichen.cloudphoto.core.auth

/**
 * Token管理器接口
 */
expect class TokenManager {
    constructor(context: Any?)
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearTokens()
    fun isLoggedIn(): Boolean
}

