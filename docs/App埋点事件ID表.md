# App 埋点事件 ID 一览（eventId → API `elementId`）

后端字段名为 **`elementId`**；产品与报表中可统称为 **eventId**，二者一一对应。

上报接口见仓库内 `CloudPhotoAPI/docs/埋点接口说明.md`（`POST /api/events`）。

## 页面标识 `page`（与路由对应）

| page（上报字段） | 说明 |
|------------------|------|
| `photo_timeline` | 照片 Tab / 时间线 |
| `albums` | 相册 Tab |
| `camera` | 拍照 Tab |
| `storage` | 存储空间 |
| `storage_add` | 添加存储配置 |
| `storage_edit` | 编辑存储配置 |
| `storage_tutorial` | 存储配置教程 WebView |
| `settings` | 我的 / 设置主页 |
| `profile` | 个人资料 |
| `account_security` | 账户安全 |
| `change_password` | 修改密码 |
| `theme_settings` | 主题设置 |
| `login` | 登录 |
| `register` | 注册 |

## 点击类事件（`eventType` = `CLICK`）

| eventId（写入 `elementId`） | 页面 `page` | elementType | 说明 |
|-----------------------------|-------------|---------------|------|
| `evt_bottom_nav_photos` | 目标 Tab 页 | tab | 底部导航 → 照片 |
| `evt_bottom_nav_albums` | 目标 Tab 页 | tab | 底部导航 → 相册 |
| `evt_bottom_nav_camera` | 目标 Tab 页 | tab | 底部导航 → 拍照 |
| `evt_bottom_nav_storage` | 目标 Tab 页 | tab | 底部导航 → 空间 |
| `evt_bottom_nav_settings` | 目标 Tab 页 | tab | 底部导航 → 我的 |
| `evt_login_submit` | `login` | button | 登录提交 |
| `evt_login_go_register` | `login` | button | 去注册 |
| `evt_register_submit` | `register` | button | 注册提交 |
| `evt_register_go_login` | `register` | button | 去登录 |
| `evt_photo_search_tap` | `photo_timeline` | button | 搜索（占位） |
| `evt_photo_grid_item` | `photo_timeline` | image | 网格点击进全屏；`position` 从 1 起；`extra` 含 `photoId` |
| `evt_photo_fullscreen_close` | `photo_timeline` | button | 关闭全屏查看 |
| `evt_settings_profile` | `settings` | list_item | 个人资料 |
| `evt_settings_account_security` | `settings` | list_item | 账户安全 |
| `evt_settings_theme` | `settings` | list_item | 主题设置 |
| `evt_settings_logout_confirm` | `settings` | button | 确认退出登录 |
| `evt_storage_add_config` | `storage` | button | 添加存储配置 |
| `evt_storage_tutorial` | `storage` | list_item | 打开配置教程 |
| `evt_storage_edit_config` | `storage` | button | 编辑某条配置；`extra` 含 `configId` |

## 浏览类事件（`eventType` = `PAGE_VIEW`）

由主导航 `LaunchedEffect` 在 **已登录** 且路由变化时自动上报：`page` 为上表路由映射，`fromPage` 为上一页面 `page`（首屏无 `fromPage`）。

## 会话与未登录队列

- **`sessionId`**：内存生成；**登录成功并写入 Token 后**会 `flush` 队列内事件并 **轮换新 session**，便于区分登录前/后路径。
- **未携带 Token 时**（登录/注册页点击）：事件进入内存队列（最多 32 条），登录成功后随 `flushPending` 上报。

## 代码常量位置

- 事件与页面常量：`shared/.../analytics/AppAnalyticsCatalog.kt`（`AnalyticsEventIds`、`AnalyticsPages`）
- 上报实现：`shared/.../analytics/AnalyticsTracker.kt`、`shared/.../service/AppEventApiService.kt`
