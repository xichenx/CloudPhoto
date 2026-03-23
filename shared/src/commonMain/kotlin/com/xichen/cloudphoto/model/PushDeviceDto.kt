package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * POST `/api/push/devices` body.
 */
@Serializable
data class RegisterPushDeviceRequest(
    val channel: String,
    val token: String,
    val deviceInstallId: String,
    val appVersion: String? = null,
    val osVersion: String? = null,
)

/**
 * `data` field of register response.
 */
@Serializable
data class PushDeviceRegisterResponseData(
    val id: String,
)
