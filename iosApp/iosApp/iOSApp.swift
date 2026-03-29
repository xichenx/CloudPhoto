import SwiftUI

/**
 * iOS 应用入口 - 遵循 Apple HIG 设计规范
 * 
 * 使用 ThemedView 包装，支持浅色/深色模式自动适配
 */
@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var mainTabDeepLinkBus = MainTabDeepLinkForegroundBus()

    var body: some Scene {
        WindowGroup {
            ThemedView {
                ContentView()
                    .environmentObject(mainTabDeepLinkBus)
            }
            .onOpenURL { url in
                if let tab = MainTabDeepLinkForegroundBus.tabIndex(for: url) {
                    mainTabDeepLinkBus.requestTabIndex(tab)
                }
            }
        }
    }
}