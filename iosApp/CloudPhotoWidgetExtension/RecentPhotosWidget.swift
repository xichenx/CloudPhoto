import SwiftUI
import WidgetKit

struct RecentPhotosWidget: Widget {
    let kind: String = "RecentPhotosWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: RecentPhotosProvider()) { entry in
            RecentPhotosWidgetEntryView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("最近照片")
        .description("预览最近照片，点击进入相册")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct RecentPhotosProvider: TimelineProvider {
    func placeholder(in context: Context) -> RecentPhotosEntry {
        RecentPhotosEntry(date: .now, snapshot: .previewLoggedIn)
    }

    func getSnapshot(in context: Context, completion: @escaping (RecentPhotosEntry) -> Void) {
        completion(RecentPhotosEntry(date: .now, snapshot: WidgetSnapshotLoader.load()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<RecentPhotosEntry>) -> Void) {
        let snap = WidgetSnapshotLoader.load()
        let entry = RecentPhotosEntry(date: .now, snapshot: snap)
        let next = Calendar.current.date(byAdding: .minute, value: 30, to: Date()) ?? Date().addingTimeInterval(1800)
        completion(Timeline(entries: [entry], policy: .after(next)))
    }
}

struct RecentPhotosEntry: TimelineEntry {
    let date: Date
    let snapshot: WidgetSnapshotPayload
}

struct RecentPhotosWidgetEntryView: View {
    var entry: RecentPhotosEntry

    var body: some View {
        let snap = entry.snapshot
        Group {
            if !snap.isLoggedIn {
                VStack(spacing: 6) {
                    Text("请登录")
                        .font(.subheadline.weight(.semibold))
                    Text("登录后查看照片")
                        .font(.caption2)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if snap.items.isEmpty {
                Text("CloudPhoto · 暂无照片")
                    .font(.caption)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if snap.items.count == 1 {
                ThumbnailView(urlString: snap.items[0].imageUrl)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                HStack(spacing: 4) {
                    ForEach(Array(snap.items.prefix(4)), id: \.id) { item in
                        ThumbnailView(urlString: item.imageUrl)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .widgetURL(URL(string: "cloudphoto://photos"))
    }
}

private struct ThumbnailView: View {
    let urlString: String

    private var thumbnailPlaceholder: some View {
        ZStack {
            Color.gray.opacity(0.2)
            Image(systemName: "photo")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    var body: some View {
        if let url = URL(string: urlString) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .clipped()
                case .failure:
                    thumbnailPlaceholder
                case .empty:
                    // Widget 里 AsyncImage 常长时间停在 empty；ProgressView 会变成「四个转圈」
                    thumbnailPlaceholder
                @unknown default:
                    thumbnailPlaceholder
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            thumbnailPlaceholder
        }
    }
}
