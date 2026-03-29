import Foundation
import WidgetKit

/// Call after Kotlin [WidgetSnapshotSync] writes the App Group JSON file.
enum WidgetTimelineReloader {
    static func reloadRecentPhotos() {
        WidgetCenter.shared.reloadTimelines(ofKind: "RecentPhotosWidget")
    }
}
