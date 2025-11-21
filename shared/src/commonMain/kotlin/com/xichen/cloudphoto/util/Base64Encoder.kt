package com.xichen.cloudphoto.util

import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.encoding.Base64

/**
 * 跨平台 Base64 编码，使用 KMP 官方 kotlin.io.encoding.Base64（commonMain 唯一实现，无需 expect/actual）。
 */
@OptIn(ExperimentalEncodingApi::class)
object Base64Encoder {
    fun encode(bytes: ByteArray): String = Base64.encode(bytes)
}
