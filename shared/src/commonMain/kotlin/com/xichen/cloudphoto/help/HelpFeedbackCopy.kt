package com.xichen.cloudphoto.help

/**
 * 帮助与反馈页的展示文案（双端共用，避免 Android / iOS 各写一份）。
 */
object HelpFeedbackCopy {

    val faqItems: List<HelpFaqItem> = listOf(
        HelpFaqItem(
            question = "如何添加云存储？",
            answer = "在底部「空间」中点击添加配置，按步骤填写云厂商的访问密钥、桶名称等信息；不确定时可先查看「配置教程」。",
        ),
        HelpFaqItem(
            question = "照片上传失败怎么办？",
            answer = "请检查网络、存储配置是否有效且为默认配置，并确认应用已获得相册/相机等必要权限。若仍失败，可通过下方表单或邮件联系我们。",
        ),
        HelpFaqItem(
            question = "反馈多久会有回复？",
            answer = "我们会尽快处理您的反馈；若填写了联系方式，必要时将通过邮件或电话与您沟通。",
        ),
    )
}

data class HelpFaqItem(
    val question: String,
    val answer: String,
)
