import Foundation

enum WidgetSnapshotLoader {
    private static let appGroupId = "group.com.xichen.cloudphoto"
    private static let fileName = "widget_snapshot.json"

    static func load() -> WidgetSnapshotPayload {
        guard let base = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroupId) else {
            return .loggedOut
        }
        let url = base.appendingPathComponent(fileName)
        guard let data = try? Data(contentsOf: url) else { return .loggedOut }
        return (try? JSONDecoder().decode(WidgetSnapshotPayload.self, from: data)) ?? .previewLoggedIn
    }
}
