package com.xichen.cloudphoto

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.xichen.cloudphoto.core.di.AppContainerHolder

/**
 * Ensures [AppContainerHolder] is initialized early for FCM token callbacks.
 */
class CloudPhotoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainerHolder.getContainer(applicationContext)
        createDefaultNotificationChannel()
    }

    private fun createDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            PUSH_CHANNEL_ID,
            getString(R.string.push_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val PUSH_CHANNEL_ID: String = "cloudphoto_default"
    }
}
