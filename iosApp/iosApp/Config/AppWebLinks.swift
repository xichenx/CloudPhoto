import Foundation

/// 与 cloudphoto-web 约定：教程 URL 须带 `client=app`，前端仅展示教程并做 App 内适配。
enum AppWebLinks {
    /// 发布前改为实际上线的官网基础地址（须含 `/tutorial?client=app`）
    static let helpTutorial = URL(string: "https://your-site.com/tutorial?client=app")!
}
