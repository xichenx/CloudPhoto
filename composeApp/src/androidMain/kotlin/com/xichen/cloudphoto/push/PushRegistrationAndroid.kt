package com.xichen.cloudphoto.push

import android.content.Context
import android.util.Log
import com.xichen.cloudphoto.core.di.AppContainerHolder

private const val TAG = "PushRegistration"

/**
 * Registers push tokens when logged in. FCM code lives on the `international` classpath only.
 */
object PushRegistrationAndroid {

    fun sync(context: Context) {
        val app = context.applicationContext
        val container = AppContainerHolder.getContainer(app)
        if (!container.tokenManager.isLoggedIn()) {
            return
        }
        VendorPushRegistration.syncVendorPushIfApplicable(app)

        if (!OemPushEnvironment.shouldRegisterFcm(app)) {
            return
        }
        syncInternationalFcmViaReflection(app)
    }

    private fun syncInternationalFcmViaReflection(context: Context) {
        try {
            val clazz = Class.forName("com.xichen.cloudphoto.push.InternationalFcmPushSyncKt")
            val method = clazz.getDeclaredMethod("syncInternationalFcmPush", Context::class.java)
            method.invoke(null, context.applicationContext)
        } catch (_: ClassNotFoundException) {
            Log.e(TAG, "InternationalFcmPushSyncKt missing; check international source set and firebase deps")
        } catch (e: ReflectiveOperationException) {
            Log.e(TAG, "FCM sync reflection failed", e)
        }
    }
}
