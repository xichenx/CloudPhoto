package com.xichen.cloudphoto.core.theme

import kotlinx.serialization.Serializable

/**
 * 应用主题配置
 */
@Serializable
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * 主题颜色配置
 */
data class AppColors(
    val primary: Long,
    val secondary: Long,
    val background: Long,
    val surface: Long,
    val error: Long,
    val onPrimary: Long,
    val onSecondary: Long,
    val onBackground: Long,
    val onSurface: Long,
    val onError: Long
)

/**
 * 默认主题颜色 - 云相册优化配色方案
 * 
 * 设计理念（参考 Google Photos、Apple Photos）：
 * - Primary: 明亮的天蓝色，代表青春、活力、清新，适合照片展示
 * - Secondary: 温暖的珊瑚红，代表热情、活力，用于强调和交互
 * - Background: 更柔和的背景色，减少视觉疲劳
 * - Surface: 纯白色，保持照片展示的清晰度
 * - 整体色调：明亮、温暖、充满活力，同时保持专业感
 */
object DefaultTheme {
    val lightColors = AppColors(
        // Primary: 明亮的天蓝色 (Sky Blue) - 青春活力的主色调
        // 参考 Google Photos 的蓝色调，适合照片应用
        primary = 0xFF00B4D8,
        // Secondary: 温暖的珊瑚红 (Coral Red) - 充满活力的辅助色
        // 用于强调和重要操作
        secondary = 0xFFFF6B6B,
        // Background: 更柔和的浅灰色，参考 Apple Photos 的背景色
        // 减少视觉疲劳，更适合长时间浏览照片
        background = 0xFFF5F5F5,
        // Surface: 纯白色，保持清晰度
        // 用于卡片和照片容器，确保照片色彩准确
        surface = 0xFFFFFFFF,
        // Error: 柔和的红色，不会过于刺眼
        error = 0xFFFF5252,
        // onPrimary: 白色文字，在天蓝色上清晰可见
        onPrimary = 0xFFFFFFFF,
        // onSecondary: 白色文字，在珊瑚红上清晰可见
        onSecondary = 0xFFFFFFFF,
        // onBackground: 深灰色文字，比纯黑更柔和
        onBackground = 0xFF1A1A1A,
        // onSurface: 深灰色文字
        onSurface = 0xFF1A1A1A,
        // onError: 白色文字
        onError = 0xFFFFFFFF
    )
    
    val darkColors = AppColors(
        // Primary: 更亮的青色，在深色背景下更突出
        // 参考 Apple Photos 深色模式的蓝色调
        primary = 0xFF4DD0E1,
        // Secondary: 更亮的珊瑚色，保持活力
        secondary = 0xFFFF8A80,
        // Background: 深色背景，参考 Google Photos 深色模式
        // 使用更深的黑色，减少眼部疲劳
        background = 0xFF000000,
        // Surface: 深灰色表面，用于卡片和容器
        // 与背景形成层次感
        surface = 0xFF1C1C1E,
        // Error: 柔和的红色
        error = 0xFFFF6B6B,
        // onPrimary: 深色文字，在亮青色上清晰可见
        onPrimary = 0xFF000000,
        // onSecondary: 深色文字
        onSecondary = 0xFF000000,
        // onBackground: 浅色文字
        onBackground = 0xFFFFFFFF,
        // onSurface: 浅色文字
        onSurface = 0xFFFFFFFF,
        // onError: 深色文字
        onError = 0xFF000000
    )
}

