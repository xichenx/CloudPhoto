package com.xichen.cloudphoto.core.auth

import platform.Foundation.NSUserDefaults

actual class TokenManager actual constructor(context: Any?) {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    private val KEY_ACCESS_TOKEN = "access_token"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    
    actual fun saveAccessToken(token: String) {
        userDefaults.setObject(token, forKey = KEY_ACCESS_TOKEN)
    }
    
    actual fun getAccessToken(): String? {
        return userDefaults.stringForKey(KEY_ACCESS_TOKEN)
    }
    
    actual fun saveRefreshToken(token: String) {
        userDefaults.setObject(token, forKey = KEY_REFRESH_TOKEN)
    }
    
    actual fun getRefreshToken(): String? {
        return userDefaults.stringForKey(KEY_REFRESH_TOKEN)
    }
    
    actual fun clearTokens() {
        userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
    }
    
    actual fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}

