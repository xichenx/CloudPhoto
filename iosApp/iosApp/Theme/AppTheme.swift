import SwiftUI
import Shared

import SwiftUI
import Shared

/**
 * iOS 主题配置 - Apple Human Interface Guidelines (HIG)
 * 
 * iOS 平台采用 Apple HIG 设计规范，参考 Apple Photos 设计：
 * - 使用 SF Symbols 图标系统
 * - 使用系统颜色和语义颜色
 * - 毛玻璃效果（Blur）和现代 iOS 设计语言
 * - 遵循 iOS 设计语言（圆角、间距、字体）
 * - 支持浅色/深色模式自动适配
 * - 使用系统组件和交互模式
 */
struct AppTheme {
    // 主题颜色 - iOS 云相册主题（参考 Apple Photos）
    struct Colors {
        // Primary: iOS 系统蓝色 - 专业可靠
        static let primary = Color.accentColor // 使用系统 Accent Color
        
        // Secondary: 柔和的绿色 - 成功状态
        static let secondary = Color(red: 0.22, green: 0.56, blue: 0.24) // iOS Green
        
        // Tertiary: 柔和的橙色 - 强调
        static let tertiary = Color(red: 0.96, green: 0.49, blue: 0.0) // iOS Orange
        
        // Error: iOS 系统红色
        static let error = Color.red
        
        // 使用系统颜色适配浅色/深色模式
        static var background: Color {
            Color(UIColor.systemBackground)
        }
        
        static var surface: Color {
            Color(UIColor.secondarySystemBackground)
        }
        
        static var text: Color {
            Color(UIColor.label)
        }
        
        static var secondaryText: Color {
            Color(UIColor.secondaryLabel)
        }
        
        // 毛玻璃效果颜色
        static var blurBackground: Color {
            Color(UIColor.systemBackground)
                .opacity(0.8)
        }
    }
    
    // 设计规范 - 遵循 Apple HIG
    struct Design {
        // 圆角半径（pt）
        /// 照片九宫格单元格，与 Android `ModernPhotoItem` 的 `RoundedCornerShape(2.dp)` 对齐。
        static let photoGridCellCornerRadius: CGFloat = 2
        static let cornerRadiusSmall: CGFloat = 8
        static let cornerRadiusMedium: CGFloat = 12
        static let cornerRadiusLarge: CGFloat = 16
        static let cornerRadiusXLarge: CGFloat = 24
        
        // 间距（pt）- 使用 4pt 的倍数
        static let spacingXS: CGFloat = 4
        static let spacingS: CGFloat = 8
        static let spacingM: CGFloat = 16
        static let spacingL: CGFloat = 24
        static let spacingXL: CGFloat = 32
        
        // 字体大小（pt）
        static let fontSizeLargeTitle: CGFloat = 34
        static let fontSizeTitle: CGFloat = 28
        static let fontSizeTitle2: CGFloat = 22
        static let fontSizeTitle3: CGFloat = 20
        static let fontSizeHeadline: CGFloat = 17
        static let fontSizeBody: CGFloat = 17
        static let fontSizeCallout: CGFloat = 16
        static let fontSizeSubheadline: CGFloat = 15
        static let fontSizeFootnote: CGFloat = 13
        static let fontSizeCaption: CGFloat = 12
        static let fontSizeCaption2: CGFloat = 11
    }
}

/**
 * 主题视图修饰符 - 应用主题配置
 * 
 * 自动适配系统浅色/深色模式，遵循 Apple HIG 设计规范
 */
struct ThemedView<Content: View>: View {
    @AppStorage("themeMode") private var themeMode: String = "system"
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .preferredColorScheme(colorScheme)
            .tint(AppTheme.Colors.primary) // 设置全局 tint 颜色
    }
    
    private var colorScheme: ColorScheme? {
        switch themeMode {
        case "light":
            return .light
        case "dark":
            return .dark
        default:
            return nil // 系统默认，自动适配
        }
    }
}

/**
 * iOS 设计系统扩展 - 现代 iOS 设计语言
 */
extension View {
    /// 毛玻璃效果卡片 - 现代 iOS 设计
    func glassCardStyle() -> some View {
        self
            .background(
                .ultraThinMaterial,
                in: RoundedRectangle(cornerRadius: AppTheme.Design.cornerRadiusLarge)
            )
            .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
    }
    
    /// 毛玻璃效果背景 - 用于导航栏等
    func glassBackground() -> some View {
        self
            .background(.ultraThinMaterial)
    }
    
    /// 应用卡片样式 - 遵循 iOS 设计规范
    func cardStyle() -> some View {
        self
            .background(AppTheme.Colors.surface)
            .cornerRadius(AppTheme.Design.cornerRadiusLarge)
            .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 2)
    }
    
    /// 应用主要按钮样式 - 遵循 iOS 设计规范
    func primaryButtonStyle() -> some View {
        self
            .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .semibold))
            .foregroundColor(.white)
            .padding(.horizontal, AppTheme.Design.spacingL)
            .padding(.vertical, AppTheme.Design.spacingM)
            .background(AppTheme.Colors.primary)
            .cornerRadius(AppTheme.Design.cornerRadiusMedium)
    }
    
    /// 应用次要按钮样式 - 遵循 iOS 设计规范
    func secondaryButtonStyle() -> some View {
        self
            .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .medium))
            .foregroundColor(AppTheme.Colors.primary)
            .padding(.horizontal, AppTheme.Design.spacingL)
            .padding(.vertical, AppTheme.Design.spacingM)
            .background(AppTheme.Colors.primary.opacity(0.1))
            .cornerRadius(AppTheme.Design.cornerRadiusMedium)
    }
    
    /// 照片网格项样式 - 与 Android 照片列表小圆角一致
    func photoGridItemStyle() -> some View {
        let r = AppTheme.Design.photoGridCellCornerRadius
        return self
            .clipShape(RoundedRectangle(cornerRadius: r, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: r, style: .continuous))
    }
}

