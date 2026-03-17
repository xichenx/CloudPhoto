package com.xichen.cloudphoto.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xichen.cloudphoto.core.theme.DefaultTheme
import com.xichen.cloudphoto.core.theme.ThemeMode

/**
 * 应用主题 - Material 3 设计系统
 * 
 * Android 平台采用 Material Design 3 设计规范：
 * - 使用 Material 3 ColorScheme（完整的颜色系统）
 * - 支持浅色/深色主题
 * - 沉浸式边到边布局
 * - Material 3 组件和交互模式
 */
@Composable
fun CloudPhotoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    // Material 3 云相册主题 - 参考 Google Photos 设计
    // 使用柔和的蓝色系，营造专业、清新的云存储体验
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            // 主色调 - 柔和的蓝色（云存储主题）
            primary = Color(0xFF64B5F6), // 浅蓝色 - 清新专业
            secondary = Color(0xFF81C784), // 绿色 - 成功/完成状态
            tertiary = Color(0xFFFFB74D), // 橙色 - 强调/警告
            background = Color(DefaultTheme.darkColors.background), // 纯黑色背景，参考 Apple Photos
            surface = Color(DefaultTheme.darkColors.surface), // 深灰色表面
            error = Color(0xFFEF5350), // 错误红色
            onPrimary = Color(0xFF000000), // 主色上的文字
            onSecondary = Color(0xFF000000),
            onTertiary = Color(0xFF000000),
            onBackground = Color(0xFFE0E0E0), // 背景上的文字
            onSurface = Color(0xFFE0E0E0), // 表面上的文字
            onError = Color(0xFFFFFFFF),
            // Container 颜色
            primaryContainer = Color(0xFF1565C0), // 深蓝色容器
            secondaryContainer = Color(0xFF2E7D32), // 深绿色容器
            tertiaryContainer = Color(0xFFE65100), // 深橙色容器
            onPrimaryContainer = Color(0xFFE3F2FD), // 主色容器上的文字
            onSecondaryContainer = Color(0xFFE8F5E9),
            onTertiaryContainer = Color(0xFFFFF3E0),
            // Surface 变体 - 参考 Google Photos 深色模式
            surfaceVariant = Color(0xFF2C2C2E), // 表面变体 - 用于卡片和容器
            onSurfaceVariant = Color(0xFFCCCCCC), // 表面变体上的文字
            // Outline - 深色主题下的边框颜色
            outline = Color(0xFF3A3A3C), // 轮廓色 - 柔和的深灰色
            outlineVariant = Color(0xFF2A2A2C) // 轮廓变体 - 更深的灰色
        )
    } else {
        lightColorScheme(
            // 主色调 - 柔和的蓝色系（云存储主题）
            primary = Color(0xFF1976D2), // Material Blue 600 - 专业可靠
            secondary = Color(0xFF388E3C), // Material Green 700 - 成功状态
            tertiary = Color(0xFFF57C00), // Material Orange 700 - 强调
            background = Color(DefaultTheme.lightColors.background), // 更柔和的浅灰色背景
            surface = Color(DefaultTheme.lightColors.surface), // 纯白表面
            error = Color(0xFFD32F2F), // 错误红色
            onPrimary = Color(0xFFFFFFFF), // 主色上的文字
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF212121), // 背景上的文字
            onSurface = Color(0xFF212121), // 表面上的文字
            onError = Color(0xFFFFFFFF),
            // Container 颜色 - 柔和的背景色
            primaryContainer = Color(0xFFE3F2FD), // 浅蓝色容器 - 清新专业
            secondaryContainer = Color(0xFFE8F5E9), // 浅绿色容器
            tertiaryContainer = Color(0xFFFFF3E0), // 浅橙色容器
            onPrimaryContainer = Color(0xFF0D47A1), // 主色容器上的深色文字
            onSecondaryContainer = Color(0xFF1B5E20),
            onTertiaryContainer = Color(0xFFE65100),
            // Surface 变体 - 参考 Google Photos 的卡片背景
            surfaceVariant = Color(0xFFF8F8F8), // 表面变体 - 更柔和的灰色，适合照片展示
            onSurfaceVariant = Color(0xFF666666), // 表面变体上的文字
            // Outline - 更柔和的边框颜色
            outline = Color(0xFFE0E0E0), // 轮廓色 - 非常柔和的灰色，几乎不可见
            outlineVariant = Color(0xFFF0F0F0) // 轮廓变体 - 极浅的灰色
        )
    }
    
    // 设置状态栏和导航栏为透明，实现沉浸式边到边布局；同步设置窗口背景，避免深色模式下跳转时白闪
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
        (view.context as? Activity)?.window?.setBackgroundDrawable(
            ColorDrawable(colorScheme.background.toArgb())
        )
    }

    // 全局禁用点击涟漪效果
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

