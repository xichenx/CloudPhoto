---
name: cloudphoto-ios-only
description: >-
  Scoped workflow for CloudPhoto iOS-only work under iosApp (SwiftUI, AppViewModel,
  NavigationCoordinator). Use when the user limits changes to iOS, or when adjusting
  SwiftUI/navigation/theme without changing shared Kotlin APIs or Android.
---

# CloudPhoto：只做 iOS

## 范围

- **主要目录**: `iosApp/iosApp/`（Views、Theme、`AppViewModel.swift`、`ContentView`、Navigation 等）
- **默认不修改**: `composeApp/`；**`shared/`** 仅在需要改 **导出给 Swift 的 Kotlin API** 或 **iosMain actual** 时再动

## 最短清单

```
- [ ] 仅 Swift / SwiftUI：不改 Kotlin 时，直接用 Xcode Build / Run 当前 Scheme
- [ ] 调用 Shared：沿用现有 `AppViewModel` 与类型；注意 Optional / 可空性
- [ ] 视觉与交互：`11-ios-swiftui.mdc` + `12-ui-design-system.mdc`（与 Android 对齐）
- [ ] 若改了 commonMain/iosMain 的 Kotlin：必须先 Gradle 产出框架再 Xcode（见下）
```

## 改了 Kotlin 时（仍算「iOS 相关」但涉及 shared）

在项目根执行（模拟器示例）：

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 -PXCODE_CONFIGURATION=DEBUG
```

然后 Xcode **Clean Build Folder** 再 Run。路径、脚本、排错：**`.cursor/rules/13-ios-kmp-run.mdc`**。

## 规则

- SwiftUI / KMP 集成：**`11-ios-swiftui.mdc`**
- 构建与 **`No such module 'Shared'`**：**`13-ios-kmp-run.mdc`**

## 何时升级工作流

- 新接口 / 新模型 / 新服务 → **shared**：[cloudphoto-shared-only](../cloudphoto-shared-only/SKILL.md)。
- 双端一起改 → [cloudphoto-cross-platform-feature](../cloudphoto-cross-platform-feature/SKILL.md)。
