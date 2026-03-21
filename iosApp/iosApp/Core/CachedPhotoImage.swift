import NukeUI
import SwiftUI

/// 远程照片展示：使用 [Nuke] 内存 / 磁盘缓存与任务去重，替代手写 URLSession 缓存。
struct CachedPhotoImage: View {
    let url: URL?
    var contentMode: ContentMode = .fill
    /// 骨架占位与网格或全屏背景对齐，默认网格。
    var skeletonPlacement: SkeletonPhotoPlaceholder.Placement = .gridCell

    var body: some View {
        Group {
            if let url {
                LazyImage(url: url) { state in
                    if let image = state.image {
                        image
                            .resizable()
                            .aspectRatio(contentMode: contentMode)
                    } else if state.error != nil {
                        Image(systemName: "photo")
                            .foregroundStyle(.secondary)
                    } else {
                        SkeletonPhotoPlaceholder(placement: skeletonPlacement)
                    }
                }
            } else {
                Image(systemName: "photo")
                    .foregroundStyle(.secondary)
            }
        }
    }
}
