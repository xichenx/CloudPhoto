package com.xichen.cloudphoto.model

import kotlinx.serialization.Serializable

/**
 * 用户反馈提交体（与业务 API `POST /api/feedback` 对齐）。
 *
 * 用户身份由服务端根据登录态（如用户 id、邮箱）解析，客户端不再传联系方式。
 *
 * @param content 反馈正文，建议服务端限制长度（如 10～2000 字）
 * @param category 业务分类：`bug` | `suggestion` | `general`
 */
@Serializable
data class FeedbackSubmitRequest(
    val content: String,
    val category: String = "general",
)
