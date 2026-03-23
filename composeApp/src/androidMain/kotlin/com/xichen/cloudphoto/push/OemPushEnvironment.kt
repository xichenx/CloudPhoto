package com.xichen.cloudphoto.push

import android.content.Context
import android.os.Build
import com.xichen.cloudphoto.BuildConfig

/**
 * 判断当前设备更适合 **FCM** 还是 **厂商推送**（国内多数机型无完整 GMS，FCM 不可用或极不稳定）。
 *
 * 策略摘要：
 * - **FCM**：仅在 `international` 构建（[BuildConfig.USE_FCM_PUSH]）且 [GmsPushAvailability] 报告 Play 服务可用时注册。
 * - **厂商**：`china` 构建不含 Firebase；按 [manufacturerKey] 在 [VendorPushRegistration] 中接各厂商 SDK。
 */
object OemPushEnvironment {

    fun manufacturerKey(): String {
        val m = Build.MANUFACTURER.orEmpty().lowercase()
        return when {
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> "xiaomi"
            m.contains("huawei") -> "huawei"
            m.contains("honor") -> "honor"
            m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> "oppo"
            m.contains("vivo") || m.contains("iqoo") -> "vivo"
            m.contains("meizu") -> "meizu"
            m.isNotEmpty() -> m
            else -> "unknown"
        }
    }

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return GmsPushAvailability.isGooglePlayServicesAvailable(context.applicationContext)
    }

    fun shouldRegisterFcm(context: Context): Boolean {
        if (!BuildConfig.USE_FCM_PUSH) {
            return false
        }
        return GmsPushAvailability.isGooglePlayServicesAvailable(context.applicationContext)
    }

    fun isMajorChinaOemDevice(): Boolean {
        return when (manufacturerKey()) {
            "xiaomi", "huawei", "honor", "oppo", "vivo", "meizu" -> true
            else -> false
        }
    }
}
