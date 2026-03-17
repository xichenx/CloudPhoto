package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 发送邮箱验证码请求
 */
@Serializable
data class SendEmailCodeRequest(
    val email: String,
    val type: String // "register", "login", "reset"
)

/**
 * 用户注册请求
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val phone: String? = null,
    val emailCode: String
)

/**
 * 用户登录请求（仅支持邮箱或手机号登录）
 */
@Serializable
data class LoginRequest(
    val account: String, // 登录账号（邮箱或手机号）
    val password: String
)

/**
 * 刷新Token请求
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 修改密码请求（需先通过邮箱验证码验证）
 */
@Serializable
data class ChangePasswordRequest(
    val email: String,
    val emailCode: String,
    val oldPassword: String,
    val newPassword: String
)

/**
 * 更新个人资料请求（仅可修改的字段）
 */
@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val avatar: String? = null
)

/**
 * 用户信息DTO
 */
@Serializable
data class UserDTO(
    val id: String,
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val avatar: String? = null,
    val createdAt: Long? = null
)

/**
 * 登录响应
 */
@Serializable
data class LoginResponse(
    val accessToken: String,
    val accessExpires: Long,
    val refreshToken: String,
    val refreshExpires: Long,
    val user: UserDTO
)

/**
 * Token刷新响应
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val accessExpires: Long,
    val refreshToken: String,
    val refreshExpires: Long
)

/**
 * API统一响应格式
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)

