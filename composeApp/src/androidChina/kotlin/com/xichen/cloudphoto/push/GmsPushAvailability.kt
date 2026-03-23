package com.xichen.cloudphoto.push

import android.content.Context

/**
 * China flavor: no Google Play services dependency; FCM is disabled at build time.
 */
internal object GmsPushAvailability {

    @Suppress("UNUSED_PARAMETER")
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return false
    }
}
