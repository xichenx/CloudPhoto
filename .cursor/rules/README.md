# CloudPhoto 开发规则和技能索引

本文档列出了 CloudPhoto 项目的所有开发规则和所需技能。

## 📋 规则文件列表

### 1. 项目结构规范 (`01-project-structure.mdc`)
- **适用范围**: 所有文件（alwaysApply: true）
- **内容**: 
  - 项目架构概览
  - 目录结构说明
  - 核心原则
  - 关键文件索引

### 2. Kotlin Multiplatform 规范 (`02-kotlin-multiplatform.mdc`)
- **适用范围**: `shared/**/*.kt`
- **内容**:
  - expect/actual 机制使用
  - 共享代码规范
  - 数据模型规范
  - 依赖管理

### 3. 网络层规范 (`03-network-layer.mdc`)
- **适用范围**: `shared/**/network/**/*.kt`, `shared/**/service/**/*.kt`
- **内容**:
  - NetworkClientFactory 使用
  - ApiResult 封装
  - 服务层实现模式
  - Token 认证

### 4. Android Compose 规范 (`04-android-compose.mdc`)
- **适用范围**: `composeApp/**/*.kt`
- **内容**:
  - Screen 组件结构
  - 导航系统
  - 状态管理
  - 权限处理
  - 响应式布局

### 5. 存储服务规范 (`05-storage-service.mdc`)
- **适用范围**: `shared/**/storage/**/*.kt`
- **内容**:
  - StorageService 接口
  - 实现新存储服务步骤
  - 签名算法规范
  - 错误处理

### 6. 依赖注入规范 (`06-dependency-injection.mdc`)
- **适用范围**: `shared/**/di/**/*.kt`, `shared/**/service/**/*.kt`
- **内容**:
  - AppContainer 使用
  - 服务获取方式
  - 生命周期管理
  - 添加新服务

### 7. 错误处理规范 (`07-error-handling.mdc`)
- **适用范围**: 所有 Kotlin 文件
- **内容**:
  - ApiResult 使用
  - ErrorHandler 统一处理
  - Result 类型使用
  - ViewModel 错误处理

### 8. 认证和 Token 管理 (`08-authentication.mdc`)
- **适用范围**: `shared/**/auth/**/*.kt`, `shared/**/service/AuthService.kt`
- **内容**:
  - TokenManager 使用
  - AuthService 流程
  - Token 刷新
  - 登录状态管理

### 9. 代码风格规范 (`09-code-style.mdc`)
- **适用范围**: 所有文件（alwaysApply: true）
- **内容**:
  - 命名约定
  - 代码格式
  - 注释规范
  - 性能优化

### 10. 测试规范 (`10-testing.mdc`)
- **适用范围**: 测试文件
- **内容**:
  - 单元测试模式
  - Mock 使用
  - 测试覆盖率目标
  - 最佳实践

### 11. iOS SwiftUI 规范 (`11-ios-swiftui.mdc`)
- **适用范围**: `iosApp/**/*.swift`
- **内容**:
  - ViewModel 结构
  - View 组件规范
  - 导航系统
  - 响应式布局
  - 主题系统
  - Toast 提示
  - KMP 集成

### 12. UI/UX 设计系统 (`12-ui-design-system.mdc`)
- **适用范围**: 所有文件（alwaysApply: true）
- **内容**:
  - 设计原则
  - 颜色系统
  - 字体系统
  - 间距系统
  - 组件规范
  - 响应式设计
  - 动画规范
  - 无障碍访问

### 13. iOS 上运行 KMP 项目 (`13-ios-kmp-run.mdc`)
- **适用范围**: `iosApp/**/*`, `shared/src/iosMain/**/*`
- **内容**:
  - 前置条件（Xcode、JDK、项目结构）
  - 命令行构建 Kotlin 框架（模拟器/真机）
  - Xcode 配置（FRAMEWORK_SEARCH_PATHS、链接 Shared、Compile Kotlin Framework 脚本）
  - 在 Xcode 中构建与运行
  - 常见问题（No such module 'Shared'、Gradle 失败、签名、类型不匹配等）
  - 验证清单

## 🎯 核心技能要求

### Kotlin 技能
- ✅ Kotlin 协程（Coroutines）
- ✅ Kotlin Flow 和 StateFlow
- ✅ Kotlinx Serialization
- ✅ expect/actual 机制
- ✅ 扩展函数和 DSL

### Android 技能
- ✅ Jetpack Compose
- ✅ ViewModel 和状态管理
- ✅ Navigation Component
- ✅ Activity Result API
- ✅ 权限处理

### iOS 技能
- ✅ SwiftUI
- ✅ Combine
- ✅ @MainActor 和异步编程
- ✅ NavigationCoordinator 模式
- ✅ UIViewControllerRepresentable
- ✅ KMP 集成和类型转换

### 架构技能
- ✅ MVVM 架构
- ✅ 依赖注入
- ✅ Repository 模式
- ✅ Service 层抽象

### 网络技能
- ✅ Ktor HttpClient
- ✅ RESTful API 设计
- ✅ JWT Token 管理
- ✅ 错误处理

### 云存储技能
- ✅ S3 兼容 API
- ✅ 签名算法（HMAC-SHA256）
- ✅ 多存储提供商支持

### UI/UX 技能
- ✅ Material Design 3
- ✅ Human Interface Guidelines
- ✅ 响应式设计
- ✅ 无障碍访问
- ✅ 设计系统构建

### 测试技能
- ✅ 单元测试
- ✅ Mock 框架（MockK）
- ✅ 协程测试
- ✅ Compose UI 测试

## 📚 参考文档

- [ARCHITECTURE.md](../ARCHITECTURE.md) - 详细架构文档
- [README.md](../README.md) - 项目说明

## 🔧 快速查找

根据任务类型查找对应规则：

| 任务 | 规则文件 |
|------|---------|
| 添加新 Screen (Android) | `04-android-compose.mdc` |
| 添加新 Screen (iOS) | `11-ios-swiftui.mdc` |
| 添加新 API | `03-network-layer.mdc` |
| 添加新存储服务 | `05-storage-service.mdc` |
| 添加新 Service | `06-dependency-injection.mdc` |
| 处理错误 | `07-error-handling.mdc` |
| 实现认证 | `08-authentication.mdc` |
| 平台特定功能 | `02-kotlin-multiplatform.mdc` |
| UI 组件设计 | `12-ui-design-system.mdc` |
| 代码风格问题 | `09-code-style.mdc` |
| 编写测试 | `10-testing.mdc` |
| iOS 构建/运行/排错 | `13-ios-kmp-run.mdc` |

## 💡 使用建议

1. **新功能开发**: 先查看 `01-project-structure.mdc` 了解架构，再查看对应的具体规则
2. **代码审查**: 参考 `09-code-style.mdc` 检查代码风格
3. **问题排查**: 根据问题类型查找对应规则文件
4. **学习路径**: 按顺序阅读规则文件，从架构到实现细节
