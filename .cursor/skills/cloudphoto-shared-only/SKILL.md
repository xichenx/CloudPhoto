---
name: cloudphoto-shared-only
description: >-
  Scoped workflow for CloudPhoto shared KMP module only (commonMain, androidMain,
  iosMain, services, models, AppContainer). Use when the user asks to change only
  shared code, refactor business logic without UI, or add expect/actual without editing
  composeApp or iosApp Swift files.
---

# CloudPhoto：只改 shared

## 范围

- **模块**: `shared/`（`commonMain`、`androidMain`、`iosMain`）
- **典型改动**: `model/`、`service/`、`core/`（网络、日志、错误、DI）、`expect`/`actual`
- **默认不修改**: `composeApp/`、`iosApp/` 下的 UI；若用户需要端到端暴露新能力，再单独开跨端任务

## 最短清单

```
- [ ] 新 DTO：`@Serializable`，时间与序列化约定见 `02-kotlin-multiplatform.mdc`
- [ ] 新服务方法：`ApiResult`/`Result` 与项目一致；HTTP 见 `03-network-layer.mdc`
- [ ] 新依赖：在 `AppContainer.kt` 注册（`06-dependency-injection.mdc`）
- [ ] expect/actual：声明在 commonMain，actual 在 androidMain/iosMain（`02-kotlin-multiplatform.mdc`）
- [ ] 验证：按下方编译任务（至少覆盖你改到的 source set）
```

## 编译验证（项目根）

**动到 commonMain 或 public API（建议两条都跑）**

```bash
./gradlew :shared:compileDebugKotlinAndroid
./gradlew :shared:compileKotlinIosSimulatorArm64
```

**仅 androidMain**：至少 `:shared:compileDebugKotlinAndroid`。  
**仅 iosMain**：至少 `:shared:compileKotlinIosSimulatorArm64`（或 Simulator 架构任务，与 `13-ios-kmp-run.mdc` 一致）。

真机架构可将任务中的 `IosSimulatorArm64` 换成 `IosArm64`。

## 规则速查

- KMP / 模型 / expect-actual：**`02-kotlin-multiplatform.mdc`**
- 网络与服务：**`03-network-layer.mdc`**
- AppContainer：**`06-dependency-injection.mdc`**
- 错误：**`07-error-handling.mdc`**

## 何时升级工作流

- 要在 **Compose / SwiftUI** 里接新状态或入口 → [cloudphoto-cross-platform-feature](../cloudphoto-cross-platform-feature/SKILL.md) 或对应 [cloudphoto-android-only](../cloudphoto-android-only/SKILL.md) / [cloudphoto-ios-only](../cloudphoto-ios-only/SKILL.md)。
