@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.xichen.cloudphoto.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * 用户是否允许服务端向本账号下发推送（与系统通知权限无关）。
 * 对应 GET/PUT [com.xichen.cloudphoto.core.config.ApiConfig.USER_PUSH_PREFERENCE_PATH] 的 `data` 与请求体。
 */
@Serializable
data class UserPushPreferenceDto(
    val pushEnabled: Boolean,
)

@Serializable
data class UserPushPreferenceUpdateRequest(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val pushEnabled: Boolean,
)
