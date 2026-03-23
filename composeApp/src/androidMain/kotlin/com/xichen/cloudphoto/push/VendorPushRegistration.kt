package com.xichen.cloudphoto.push

import android.content.Context
import com.xichen.cloudphoto.BuildConfig
import com.xichen.cloudphoto.analytics.analyticsDeviceMeta
import com.xichen.cloudphoto.core.di.AppContainerHolder
import com.xichen.cloudphoto.model.RegisterPushDeviceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 厂商推送 Token 注册入口。国内分发需在对应厂商开放平台申请应用并集成各 SDK 后，在此按 [OemPushEnvironment.manufacturerKey] 分支实现。
 *
 * 上报时使用与后端一致的 `channel`：`xiaomi` / `huawei` / `honor` / `oppo` / `vivo` / `meizu`（小写）。
 */
object VendorPushRegistration {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 在登录态下，将厂商返回的 Token 写入后端（与 FCM 并行或单独使用，取决于业务）。
     */
    fun registerVendorToken(context: Context, channel: String, token: String) {
        val app = context.applicationContext
        val container = AppContainerHolder.getContainer(app)
        if (!container.tokenManager.isLoggedIn() || token.isBlank()) {
            return
        }
        scope.launch {
            val meta = analyticsDeviceMeta(app)
            val installId = pushInstallId(app)
            container.pushDeviceApiService.registerOutcome(
                RegisterPushDeviceRequest(
                    channel = channel.lowercase(),
                    token = token,
                    deviceInstallId = installId,
                    appVersion = meta.appVersion,
                    osVersion = meta.osVersion,
                ),
            )
        }
    }

    /**
     * 初始化厂商 SDK 并拉取 Token。当前为占位：接入小米推送、华为 Push、OPPO / vivo 等 SDK 后在此实现。
     */
    @Suppress("UNUSED_PARAMETER")
    fun syncVendorPushIfApplicable(context: Context) {
        if (!BuildConfig.USE_VENDOR_PUSH) {
            return
        }
        if (!OemPushEnvironment.isMajorChinaOemDevice()) {
            return
        }
        when (OemPushEnvironment.manufacturerKey()) {
            "xiaomi" -> { /* TODO: MiPushClient.registerPush(context, appId, appKey) → registerVendorToken(..., "xiaomi", regId) */ }
            "huawei" -> { /* TODO: HmsMessaging.getInstance(context).token.addOnSuccessListener { registerVendorToken(..., "huawei", it) } */ }
            "honor" -> { /* TODO: 荣耀推送 SDK，channel 建议 "honor" */ }
            "oppo" -> { /* TODO: OPPO 推送 */ }
            "vivo" -> { /* TODO: vivo 推送 */ }
            "meizu" -> { /* TODO: 魅族推送 */ }
            else -> { /* 其它厂商或未知 */ }
        }
    }
}
