package com.xichen.cloudphoto.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles FCM token refresh and foreground messages (international flavor only).
 */
class CloudPhotoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        PushRegistrationAndroid.sync(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}
