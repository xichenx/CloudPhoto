import Combine
import SwiftUI

/// Routes `cloudphoto://` URLs from widgets / shortcuts into the main tab bar.
@MainActor
final class MainTabDeepLinkForegroundBus: ObservableObject {
    @Published var requestedTabIndex: Int?

    func requestTabIndex(_ index: Int) {
        requestedTabIndex = index
    }

    static func tabIndex(for url: URL) -> Int? {
        guard url.scheme == "cloudphoto" else { return nil }
        switch url.host {
        case "photos": return 0
        case "camera": return 2
        default: return nil
        }
    }
}
