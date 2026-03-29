import Foundation

/// Mirrors Kotlin [WidgetSnapshotPayload] JSON in App Group `widget_snapshot.json`.
struct WidgetSnapshotPayload: Codable {
    let schemaVersion: Int
    let updatedAtEpochMillis: Int64
    let photoCount: Int
    let items: [WidgetSnapshotItem]
    /// Omitted in older snapshots: treat as logged in so we do not flash a false sign-in state.
    let isLoggedIn: Bool

    enum CodingKeys: String, CodingKey {
        case schemaVersion
        case updatedAtEpochMillis
        case photoCount
        case items
        case isLoggedIn
    }

    /// Widget gallery / placeholder: logged in, no photos.
    static let previewLoggedIn = WidgetSnapshotPayload(
        schemaVersion: 1,
        updatedAtEpochMillis: 0,
        photoCount: 0,
        items: [],
        isLoggedIn: true
    )

    /// No snapshot file or not yet synced — prompt sign-in.
    static let loggedOut = WidgetSnapshotPayload(
        schemaVersion: 1,
        updatedAtEpochMillis: 0,
        photoCount: 0,
        items: [],
        isLoggedIn: false
    )

    init(schemaVersion: Int, updatedAtEpochMillis: Int64, photoCount: Int, items: [WidgetSnapshotItem], isLoggedIn: Bool = true) {
        self.schemaVersion = schemaVersion
        self.updatedAtEpochMillis = updatedAtEpochMillis
        self.photoCount = photoCount
        self.items = items
        self.isLoggedIn = isLoggedIn
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        schemaVersion = try c.decodeIfPresent(Int.self, forKey: .schemaVersion) ?? 1
        updatedAtEpochMillis = try c.decodeIfPresent(Int64.self, forKey: .updatedAtEpochMillis) ?? 0
        photoCount = try c.decodeIfPresent(Int.self, forKey: .photoCount) ?? 0
        items = try c.decodeIfPresent([WidgetSnapshotItem].self, forKey: .items) ?? []
        isLoggedIn = try c.decodeIfPresent(Bool.self, forKey: .isLoggedIn) ?? true
    }
}

struct WidgetSnapshotItem: Codable {
    let id: String
    let imageUrl: String
    let name: String
}
