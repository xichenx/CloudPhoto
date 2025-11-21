# 平台设计系统规范

## 概述

CloudPhoto 项目采用**平台原生设计系统**，确保每个平台都遵循其官方设计规范，提供最佳的用户体验。

- **Android**: Material Design 3
- **iOS**: Apple Human Interface Guidelines (HIG)

## Android - Material Design 3

### 设计原则

1. **Material You**: 动态颜色系统，个性化体验
2. **Material 3 组件**: 使用最新的 Material 3 组件库
3. **边到边布局**: 沉浸式设计，内容延伸到系统栏
4. **响应式设计**: 适配不同屏幕尺寸和方向

### 颜色系统

#### 主色调
- **Primary**: `#00B4D8` (天蓝色) - 青春活力的主色调
- **Secondary**: `#FF6B6B` (珊瑚红) - 温暖活力的辅助色
- **Tertiary**: `#4ECDC4` (薄荷绿) - 清新活力的第三色

#### Material 3 完整颜色系统
```kotlin
lightColorScheme(
    primary = Color(0xFF00B4D8),
    secondary = Color(0xFFFF6B6B),
    tertiary = Color(0xFF4ECDC4),
    primaryContainer = Color(0xFFB3E5FC),
    secondaryContainer = Color(0xFFFFE0E0),
    tertiaryContainer = Color(0xFFB2DFDB),
    // ... 其他颜色
)
```

### 组件规范

#### 按钮
- **主要按钮**: 使用 `Button`，高度 56dp，圆角 18dp
- **次要按钮**: 使用 `OutlinedButton`，高度 56dp
- **文本按钮**: 使用 `TextButton`

#### 卡片
- **圆角**: 16-28dp（根据重要性）
- **阴影**: 4-12dp elevation
- **内边距**: 20-28dp

#### 输入框
- **圆角**: 18dp
- **高度**: 56dp
- **聚焦状态**: Primary 颜色边框
- **错误状态**: Error 颜色边框和图标

#### 导航栏
- **底部导航**: 使用 `NavigationBar`
- **顶部应用栏**: 使用 `TopAppBar`
- **圆角**: 顶部 24dp

### 字体系统

使用 Material 3 Typography：
- **Display Large**: 57sp
- **Headline Large**: 32sp
- **Title Large**: 22sp
- **Body Large**: 16sp
- **Label Large**: 14sp

### 间距系统

使用 4dp 的倍数：
- **4dp**: 最小间距
- **8dp**: 小间距
- **16dp**: 标准间距
- **24dp**: 大间距
- **32dp**: 超大间距

### 动画

- **页面切换**: 300ms
- **状态变化**: 200ms
- **加载动画**: 使用 `CircularProgressIndicator`

## iOS - Apple Human Interface Guidelines

### 设计原则

1. **清晰度**: 清晰的视觉层次和焦点
2. **一致性**: 使用系统组件和交互模式
3. **深度**: 通过层次和动画传达层次感
4. **尊重内容**: 内容优先，界面不喧宾夺主

### 颜色系统

#### 主色调（与 Android 保持一致）
- **Primary**: `RGB(0, 180, 216)` - 天蓝色
- **Secondary**: `RGB(255, 107, 107)` - 珊瑚红
- **Tertiary**: `RGB(78, 205, 196)` - 薄荷绿
- **Error**: `RGB(255, 82, 82)` - 柔和红

#### 系统颜色（自动适配浅色/深色模式）
```swift
Color(UIColor.systemBackground)      // 背景色
Color(UIColor.secondarySystemBackground) // 表面色
Color(UIColor.label)                 // 文字色
Color(UIColor.secondaryLabel)        // 次要文字色
```

### 组件规范

#### 按钮
- **主要按钮**: 使用 `.buttonStyle(.borderedProminent)`
- **次要按钮**: 使用 `.buttonStyle(.bordered)`
- **高度**: 44pt（最小触摸目标）
- **圆角**: 12pt

#### 卡片
- **圆角**: 16pt
- **阴影**: `shadow(color:opacity:radius:x:y:)`
- **内边距**: 16-24pt

#### 输入框
- **使用**: `TextField` 和 `SecureField`
- **样式**: `.textFieldStyle(.roundedBorder)` 或自定义
- **圆角**: 8pt

#### 导航
- **Tab Bar**: 使用 `TabView`
- **Navigation Bar**: 使用 `NavigationView` 和 `.navigationTitle()`
- **SF Symbols**: 使用系统图标

### 字体系统

使用 San Francisco 字体（系统默认）：
- **Large Title**: 34pt
- **Title**: 28pt
- **Title 2**: 22pt
- **Title 3**: 20pt
- **Headline**: 17pt (Semibold)
- **Body**: 17pt (Regular)
- **Callout**: 16pt
- **Subheadline**: 15pt
- **Footnote**: 13pt
- **Caption**: 12pt
- **Caption 2**: 11pt

### 间距系统

使用 4pt 的倍数：
- **4pt**: 最小间距
- **8pt**: 小间距
- **16pt**: 标准间距
- **24pt**: 大间距
- **32pt**: 超大间距

### 圆角系统

- **Small**: 8pt
- **Medium**: 12pt
- **Large**: 16pt
- **XLarge**: 24pt

### SF Symbols

使用系统图标库：
- **照片**: `photo`, `photo.fill`
- **相册**: `photo.on.rectangle`
- **相机**: `camera`, `camera.fill`
- **设置**: `gearshape`, `gearshape.fill`
- **用户**: `person`, `person.circle`, `person.fill`

### 动画

- **页面切换**: 使用系统默认动画
- **状态变化**: `.animation(.easeInOut)`
- **加载动画**: 使用 `ProgressView()`

## 设计差异对比

| 特性 | Android (Material 3) | iOS (HIG) |
|------|---------------------|-----------|
| **主按钮高度** | 56dp | 44pt |
| **卡片圆角** | 16-28dp | 16pt |
| **输入框圆角** | 18dp | 8pt |
| **导航栏** | Bottom Navigation Bar | Tab Bar |
| **图标系统** | Material Icons | SF Symbols |
| **字体** | Roboto (Material) | San Francisco (系统) |
| **最小触摸目标** | 48dp | 44pt |
| **阴影** | Elevation (4-12dp) | Shadow (radius, opacity) |
| **颜色适配** | Material 3 ColorScheme | System Colors |

## 实现文件

### Android
- `composeApp/src/androidMain/kotlin/com/xichen/cloudphoto/theme/Theme.kt`
- `shared/src/commonMain/kotlin/com/xichen/cloudphoto/core/theme/AppTheme.kt`

### iOS
- `iosApp/iosApp/Theme/AppTheme.swift`
- `iosApp/iosApp/ContentView.swift`

## 最佳实践

### Android
1. ✅ 使用 Material 3 组件（Button, Card, TextField 等）
2. ✅ 遵循 Material 3 颜色系统
3. ✅ 使用 Material Icons
4. ✅ 实现边到边布局
5. ✅ 使用 Material 3 Typography

### iOS
1. ✅ 使用系统组件（Button, List, Form 等）
2. ✅ 使用 SF Symbols 图标
3. ✅ 使用系统颜色（自动适配浅色/深色模式）
4. ✅ 遵循 iOS 间距和圆角规范
5. ✅ 使用系统字体（San Francisco）

## 颜色一致性

虽然两个平台使用不同的设计系统，但**主色调保持一致**，确保品牌识别度：

- **Primary**: 天蓝色 `#00B4D8` / `RGB(0, 180, 216)`
- **Secondary**: 珊瑚红 `#FF6B6B` / `RGB(255, 107, 107)`
- **Tertiary**: 薄荷绿 `#4ECDC4` / `RGB(78, 205, 196)`

这样既保持了品牌一致性，又遵循了各平台的设计规范。

## 总结

- **Android**: 完全遵循 Material Design 3 规范，使用 Material 3 组件和颜色系统
- **iOS**: 完全遵循 Apple HIG 规范，使用系统组件、SF Symbols 和系统颜色
- **一致性**: 主色调保持一致，确保品牌识别度
- **原生体验**: 每个平台都提供最佳的原生用户体验
