import Foundation

/// 设置 Tab 内二级路由，用于根据是否在根界面控制 Tab 栏显隐（与 StorageRoute 一致）
enum SettingsRoute: Hashable {
    case profile
    case accountSecurity
    case changePassword
    case themeSettings
}
