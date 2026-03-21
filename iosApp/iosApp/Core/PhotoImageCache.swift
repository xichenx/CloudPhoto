import SwiftUI
import UIKit

// MARK: - 内存缓存 + HTTP 磁盘缓存（URLCache，Caches 内 LRU）+ 同 URL 并发合并

enum PhotoImageCacheError: Error {
    case invalidImageData
}

/// 全进程单例：内存 [NSCache] + [URLSession] 的 [URLCache]；相同 URL 同时进行中的加载只发一次网络请求。
final class PhotoImageCache: @unchecked Sendable {
    static let shared = PhotoImageCache()

    private let memory = NSCache<NSString, UIImage>()
    private let lock = NSLock()
    private var inflight: [URL: Task<UIImage, Error>] = [:]
    private let session: URLSession

    private init() {
        memory.countLimit = 120
        memory.totalCostLimit = 80 * 1024 * 1024

        let memoryBytes = 32 * 1024 * 1024
        let diskBytes = 120 * 1024 * 1024
        let cacheFolder = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
            .appendingPathComponent("PhotoImageHttpCache", isDirectory: true)
        try? FileManager.default.createDirectory(at: cacheFolder, withIntermediateDirectories: true)

        let urlCache = URLCache(memoryCapacity: memoryBytes, diskCapacity: diskBytes, directory: cacheFolder)
        let config = URLSessionConfiguration.default
        config.urlCache = urlCache
        config.requestCachePolicy = .returnCacheDataElseLoad
        session = URLSession(configuration: config)
    }

    func image(for url: URL) async throws -> UIImage {
        let key = url.absoluteString as NSString
        if let cached = memory.object(forKey: key) {
            return cached
        }

        lock.lock()
        if let task = inflight[url] {
            lock.unlock()
            return try await task.value
        }
        let task = Task<UIImage, Error> {
            let (data, _) = try await PhotoImageCache.shared.session.data(from: url)
            guard let image = UIImage(data: data) else {
                throw PhotoImageCacheError.invalidImageData
            }
            PhotoImageCache.shared.memory.setObject(image, forKey: key, cost: data.count)
            return image
        }
        inflight[url] = task
        lock.unlock()

        do {
            let image = try await task.value
            lock.lock()
            inflight[url] = nil
            lock.unlock()
            return image
        } catch {
            lock.lock()
            inflight[url] = nil
            lock.unlock()
            throw error
        }
    }
}

// MARK: - SwiftUI（替换多处 AsyncImage，共用内存 + HTTP 缓存命中）

struct CachedPhotoImage: View {
    let url: URL?
    var contentMode: ContentMode = .fill
    /// 骨架占位与网格或全屏背景对齐，默认网格。
    var skeletonPlacement: SkeletonPhotoPlaceholder.Placement = .gridCell

    @State private var uiImage: UIImage?
    @State private var loadFailed = false

    var body: some View {
        Group {
            if let uiImage {
                Image(uiImage: uiImage)
                    .resizable()
                    .aspectRatio(contentMode: contentMode)
            } else if loadFailed {
                Image(systemName: "photo")
                    .foregroundStyle(.secondary)
            } else {
                SkeletonPhotoPlaceholder(placement: skeletonPlacement)
            }
        }
        .task(id: url?.absoluteString) {
            uiImage = nil
            loadFailed = false
            guard let url else {
                loadFailed = true
                return
            }
            do {
                uiImage = try await PhotoImageCache.shared.image(for: url)
            } catch {
                loadFailed = true
            }
        }
    }
}
