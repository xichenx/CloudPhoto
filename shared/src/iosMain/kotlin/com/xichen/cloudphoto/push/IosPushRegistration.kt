package com.xichen.cloudphoto.push

import com.xichen.cloudphoto.analytics.analyticsDeviceMeta
import com.xichen.cloudphoto.core.di.AppContainerHolder
import com.xichen.cloudphoto.model.RegisterPushDeviceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Bridges APNs token registration from Swift; runs API calls off the main thread.
 */
object IosPushRegistration {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Uploads APNs device token (hex) when user is logged in.
     */
    fun syncApnsTokenIfLoggedIn(tokenHex: String) {
        val container = AppContainerHolder.getContainer(null)
        if (!container.tokenManager.isLoggedIn()) {
            return
        }
        scope.launch {
            val meta = analyticsDeviceMeta(null)
            val installId = pushInstallId(null)
            container.pushDeviceApiService.registerOutcome(
                RegisterPushDeviceRequest(
                    channel = "apns",
                    token = tokenHex,
                    deviceInstallId = installId,
                    appVersion = meta.appVersion,
                    osVersion = meta.osVersion,
                ),
            )
        }
    }

    /**
     * Called after login success to register if system already delivered a token.
     */
    fun syncApnsTokenAfterLogin(tokenHex: String?) {
        if (tokenHex.isNullOrBlank()) {
            return
        }
        syncApnsTokenIfLoggedIn(tokenHex)
    }

    /**
     * Removes push rows for this install; invoke with a valid access token **before** clearing tokens.
     */
    fun unregisterOnLogout(done: () -> Unit) {
        val container = AppContainerHolder.getContainer(null)
        scope.launch {
            try {
                if (container.tokenManager.isLoggedIn()) {
                    val installId = pushInstallId(null)
                    container.pushDeviceApiService.unregisterOutcome(installId)
                }
            } finally {
                done()
            }
        }
    }
}
