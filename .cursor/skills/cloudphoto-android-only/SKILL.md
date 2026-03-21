---
name: cloudphoto-android-only
description: >-
  Scoped workflow for CloudPhoto Android-only work under composeApp (Compose UI,
  navigation, AppViewModel wiring). Use when the user or task explicitly limits changes
  to Android, or when fixing Android-only UI/navigation/permissions without touching iOS
  or shared business APIs.
---

# CloudPhoto：只做 Android

## 范围

- **主要目录**: `composeApp/src/androidMain/`
- **状态与用例**: `AppViewModel.kt`、各 `ui/*Screen.kt`、导航、`theme/`
- **默认不修改**: `iosApp/`；`shared/` 仅在「需要新 API / 新状态源」时再动（否则用 [cloudphoto-shared-only](../cloudphoto-shared-only/SKILL.md) 或 [cloudphoto-cross-platform-feature](../cloudphoto-cross-platform-feature/SKILL.md)）

## 最短清单

```
- [ ] UI / 导航 / 字符串：仅在 composeApp 内完成
- [ ] 调用已有 shared 能力：经 AppViewModel 或已有注入，不在 Composable 里 new 长生命周期客户端
- [ ] 权限 / Activity 结果：按现有模块与 `04-android-compose.mdc`
- [ ] 视觉：`12-ui-design-system.mdc`（与 iOS 配色一致）
- [ ] 验证：`./gradlew :composeApp:assembleDebug`（或 IDE Build）
```

## 规则

- Compose / 导航 / 权限：**`.cursor/rules/04-android-compose.mdc`**
- 若误触业务契约：回看 **`03-network-layer.mdc`**、**`07-error-handling.mdc`**

## 何时升级工作流

- 要新增 **HTTP 接口、DTO、AppContainer 服务** → 用 **shared**（[cloudphoto-shared-only](../cloudphoto-shared-only/SKILL.md)）再回连 Android。
- 要 **双端一致行为** → [cloudphoto-cross-platform-feature](../cloudphoto-cross-platform-feature/SKILL.md)。
