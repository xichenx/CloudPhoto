import Foundation

/// 存储 Tab 内二级路由，用于根据是否在根界面控制 Tab 栏显隐（与 Android 一致：添加 / 编辑 / 配置教程）
enum StorageRoute: Hashable {
    case addConfig
    case editConfig(configId: String)
    case configTutorial
}
