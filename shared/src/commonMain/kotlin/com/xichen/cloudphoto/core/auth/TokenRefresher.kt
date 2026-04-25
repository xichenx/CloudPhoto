package com.xichen.cloudphoto.core.auth

import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.service.AuthService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

/**
 * TokenRefresher performs a "silent refresh" using refresh token.
 *
 * - If refresh succeeds: persists new access/refresh tokens via [TokenManager]
 * - If refresh fails: returns false; caller may choose to force logout
 *
 * This object is shared (KMP) and intentionally does not depend on UI layer.
 */
object TokenRefresher {
    private val lock = Mutex()

    @Volatile
    private var tokenManagerProvider: (() -> TokenManager)? = null

    @Volatile
    private var authServiceProvider: (() -> AuthService)? = null

    fun install(
        tokenManagerProvider: () -> TokenManager,
        authServiceProvider: () -> AuthService
    ) {
        this.tokenManagerProvider = tokenManagerProvider
        this.authServiceProvider = authServiceProvider
    }

    suspend fun tryRefresh(): Boolean = lock.withLock {
        val tm = tokenManagerProvider?.invoke() ?: return@withLock false
        val svc = authServiceProvider?.invoke() ?: return@withLock false

        val refreshToken = tm.getRefreshToken() ?: return@withLock false
        return@withLock when (val res = svc.refreshToken(refreshToken)) {
            is ApiResult.Success -> {
                tm.saveAccessToken(res.data.accessToken)
                tm.saveRefreshToken(res.data.refreshToken)
                true
            }
            else -> false
        }
    }
}

