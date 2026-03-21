---
name: cloudphoto-cross-platform-feature
description: >-
  Guides end-to-end changes in the CloudPhoto KMP repo (shared → DI → Android Compose +
  iOS SwiftUI) with verification commands. Use when adding or changing features, APIs,
  screens, ViewModels, expect/actual, or when the user asks for cross-platform workflow,
  implementation order, or build verification for Android and iOS together.
---

# CloudPhoto 跨端功能工作流

## 何时打开本 Skill

- 新功能需要 **Android + iOS** 行为一致，或涉及 **shared** 与双端 UI。
- 不确定改动应落在 `shared`、`composeApp` 还是 `iosApp`。
- 需要 **推荐的编译/验证顺序**（尤其 iOS 与 Kotlin 框架）。

## 轻量 Skill（单域）

- 仅 Android：[cloudphoto-android-only](../cloudphoto-android-only/SKILL.md)
- 仅 iOS：[cloudphoto-ios-only](../cloudphoto-ios-only/SKILL.md)
- 仅 shared：[cloudphoto-shared-only](../cloudphoto-shared-only/SKILL.md)

## 核心原则（短）

1. **业务与网络默认在 `shared/commonMain`**；平台 API 用 **expect/actual**。
2. **新服务进 `AppContainer`**，View / Composable 不私自创建长期存活的客户端。
3. **双端各有一层 ViewModel（或等价）** 调用 shared，UI 保持薄。
4. 改完按下方清单 **至少验证到「shared 编译 + 目标平台构建」**。

## 工作流 A：新 API / 新业务能力

复制进度：

```
- [ ] 模型/DTO（shared/model，@Serializable）
- [ ] Service 方法（shared/service，suspend，ApiResult/Result 与项目一致）
- [ ] AppContainer 注册与注入路径
- [ ] 错误与用户可见文案（ErrorHandler / 统一 Toast 风格）
- [ ] Android：AppViewModel 或 Screen 状态
- [ ] iOS：AppViewModel / 视图状态
- [ ] 验证命令（见下）
```

**注意**：HTTP 细节遵循 `.cursor/rules/03-network-layer.mdc`；Token 与登录相关先看 `08-authentication.mdc`。

## 工作流 B：仅 UI / 交互（无新 API）

- **Android**：`composeApp/.../ui/`，导航见 `04-android-compose.mdc`。
- **iOS**：`iosApp/iosApp/...`，导航与主题见 `11-ios-swiftui.mdc`。
- 若需要从 shared 读配置或文案，优先 **已有** `AppConfig` / 服务，避免在 UI 硬编码环境相关常量。

## 工作流 C：平台能力（文件、权限、日志、缓存路径等）

1. 在 **commonMain** 声明 `expect`（接口或类）。
2. 在 **androidMain** / **iosMain** 写 `actual`。
3. 在 **AppContainer** 或工厂中装配，供 service 使用。
4. 参考现有：`core/logger`、`core/permission`、`TokenManager` 等实现风格（`02-kotlin-multiplatform.mdc`）。

## 验证命令（项目根目录）

**Shared / Kotlin（改 API 后必做，再编 iOS）**

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:compileDebugKotlinAndroid
```

**Android 应用**

```bash
./gradlew :composeApp:assembleDebug
```

**iOS 框架（模拟器 Debug 示例）**

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 -PXCODE_CONFIGURATION=DEBUG
```

完整 Xcode 流程、路径与排错：阅读 `.cursor/rules/13-ios-kmp-run.mdc`（不要凭记忆猜 Framework Search Paths）。

## 规则速查（深入细节时读对应 .mdc）

| 主题 | 规则 |
|------|------|
| 目录与关键文件 | `01-project-structure.mdc` |
| expect/actual、模型、依赖 | `02-kotlin-multiplatform.mdc` |
| 网络与 Service | `03-network-layer.mdc` |
| AppContainer | `06-dependency-injection.mdc` |
| ApiResult / 错误 | `07-error-handling.mdc` |
| Compose | `04-android-compose.mdc` |
| SwiftUI | `11-ios-swiftui.mdc` |
| 视觉规范 | `12-ui-design-system.mdc` |
| 测试 | `10-testing.mdc` |

架构总览：`ARCHITECTURE.md`（仓库根目录）。

## PR / 交付前自检（可选）

- [ ] 未在客户端记录 Token、密码或完整鉴权头（见安全相关 user rules）。
- [ ] 双端用户路径对同一错误有 **等价** 的提示粒度（不要一端暴露细节一端静默）。
- [ ] 若改动了 Swift 可见的 Kotlin API：已重新编译 iOS 框架并通过 Xcode Build。
