package com.xichen.cloudphoto.push

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.xichen.cloudphoto.analytics.analyticsDeviceMeta
import com.xichen.cloudphoto.core.di.AppContainerHolder
import com.xichen.cloudphoto.model.RegisterPushDeviceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val internationalFcmScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/**
 * FCM token fetch and backend registration (international flavor classpath only).
 */
fun syncInternationalFcmPush(context: Context) {
    val app = context.applicationContext
    val container = AppContainerHolder.getContainer(app)
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful || task.result == null) {
            return@addOnCompleteListener
        }
        val token = task.result!!
        internationalFcmScope.launch {
            val meta = analyticsDeviceMeta(app)
            val installId = pushInstallId(app)
            container.pushDeviceApiService.registerOutcome(
                RegisterPushDeviceRequest(
                    channel = "fcm",
                    token = token,
                    deviceInstallId = installId,
                    appVersion = meta.appVersion,
                    osVersion = meta.osVersion,
                ),
            )
        }
    }
}
