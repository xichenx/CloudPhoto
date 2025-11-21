package com.xichen.cloudphoto.core.auth

import android.content.Context
import android.content.SharedPreferences

actual class TokenManager {
    private val androidContext: Context
    private val prefs: SharedPreferences
    
    // actual constructor 委托给辅助构造函数
    actual constructor(context: Any?) : this(context as? Context 
        ?: throw IllegalArgumentException("TokenManager requires Android Context"))
    
    // 辅助构造函数用于实际初始化
    constructor(context: Context) {
        this.androidContext = context
        this.prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }
    
    private val KEY_ACCESS_TOKEN = "access_token"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    
    actual fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }
    
    actual fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    actual fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }
    
    actual fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    actual fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }
    
    actual fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}
