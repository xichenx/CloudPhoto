package com.xichen.cloudphoto.push

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Google Play services availability for FCM (international flavor only).
 */
internal object GmsPushAvailability {

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val code = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context.applicationContext)
        return code == ConnectionResult.SUCCESS
    }
}
