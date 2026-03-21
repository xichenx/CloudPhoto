import SkeletonView
import SwiftUI
import UIKit
import Shared

/**
 * 全屏照片查看：贴近 iOS 系统「照片」大图页
 * - 浅色画布、UIPageViewController 横向翻页、UIScrollView 捏合 / 双击缩放
 * - 顶栏：圆形返回、中央信息胶囊、更多菜单；单击画面显隐 Chrome
 * - 双击：放大至铺满可视区域（裁切留白）并隐藏顶/底操作区；再双击恢复适配并显示操作区
 * - 底栏：横向缩略图条 + 工具栏（分享 / 收藏 / 信息 / 编辑 / 删除）
 * - 仅通过顶栏返回按钮关闭（无下滑关闭）
 */
struct PhotoFullscreenViewer: View {
    let photos: [Photo]
    @State private var currentPage: Int
    @State private var chromeHidden: Bool = false
    @State private var favoriteIds: Set<String> = []
    @State private var showInfo = false
    @State private var showDeleteConfirm = false
    @State private var showEditPlaceholder = false
    let onDismiss: () -> Void
    var onDeletePhoto: ((String) -> Void)?

    init(
        photos: [Photo],
        initialIndex: Int,
        onDismiss: @escaping () -> Void,
        onDeletePhoto: ((String) -> Void)? = nil
    ) {
        self.photos = photos
        let safe = photos.isEmpty ? 0 : min(max(initialIndex, 0), photos.count - 1)
        _currentPage = State(initialValue: safe)
        self.onDismiss = onDismiss
        self.onDeletePhoto = onDeletePhoto
    }

    private var urlStrings: [String] {
        photos.map { String(describing: $0.url) }
    }

    private var currentPhoto: Photo? {
        guard currentPage >= 0, currentPage < photos.count else { return nil }
        return photos[currentPage]
    }

    var body: some View {
        Group {
            if photos.isEmpty {
                Color(UIColor.systemBackground).ignoresSafeArea()
            } else {
                // 底图铺满屏幕；顶/底栏不 ignoresSafeArea，由 SwiftUI 只算一次安全区。
                // 切勿再叠加 keyWindow.safeAreaInsets.top，否则会与 overlay 安全区重复，出现「像多了一截 toolbar」的大空白。
                ZStack {
                    PhotoFullscreenHostRepresentable(
                        urlStrings: urlStrings,
                        initialPage: currentPage,
                        currentPage: $currentPage,
                        onSingleTap: {
                            withAnimation(.easeInOut(duration: 0.22)) {
                                chromeHidden.toggle()
                            }
                        },
                        onDoubleTapFillChrome: { hideChrome in
                            withAnimation(.easeInOut(duration: 0.22)) {
                                chromeHidden = hideChrome
                            }
                        }
                    )
                    .ignoresSafeArea()

                    if !chromeHidden {
                        VStack(spacing: 0) {
                            topChrome
                                .padding(.top, AppTheme.Design.spacingXS)
                            Spacer(minLength: 0)
                                .allowsHitTesting(false)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                        .transition(.opacity)
                    }

                    if !chromeHidden {
                        VStack(spacing: 0) {
                            Spacer(minLength: 0)
                                .allowsHitTesting(false)
                            bottomChrome
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                    }
                }
                .animation(.easeInOut(duration: 0.22), value: chromeHidden)
            }
        }
        .background(Color(UIColor.systemBackground))
        .toolbar(.hidden, for: .navigationBar)
        .alert("照片信息", isPresented: $showInfo, presenting: currentPhoto) { _ in
            Button("好", role: .cancel) {}
        } message: { photo in
            Text(infoMessage(for: photo))
        }
        .alert("编辑", isPresented: $showEditPlaceholder) {
            Button("好", role: .cancel) {}
        } message: {
            Text("云端相册暂不支持在系统编辑器中打开，请使用其他应用编辑后重新上传。")
        }
        .alert("删除照片？", isPresented: $showDeleteConfirm) {
            Button("删除", role: .destructive) {
                if let id = currentPhoto?.id {
                    onDeletePhoto?(id)
                    if photos.count <= 1 {
                        onDismiss()
                    }
                }
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("此操作将从云端移除该照片，且无法撤销。")
        }
        .onChange(of: photos.count) { _, newCount in
            guard newCount > 0, currentPage >= newCount else { return }
            currentPage = newCount - 1
        }
        .onChange(of: currentPage) { _, _ in
            chromeHidden = false
        }
    }

    // MARK: - Top (系统照片风格)

    private var topChrome: some View {
        HStack(alignment: .center, spacing: 10) {
            Button(action: onDismiss) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(Color(UIColor.label))
                    .frame(width: 40, height: 40)
                    .background(photosChromeCircleFill, in: Circle())
                    .shadow(color: .black.opacity(0.12), radius: 4, x: 0, y: 2)
            }
            .accessibilityLabel("返回")

            Spacer(minLength: 6)

            if let photo = currentPhoto {
                centerMetaPill(for: photo)
            }

            Spacer(minLength: 6)

            Menu {
                Button {
                    showInfo = true
                } label: {
                    Label("显示信息", systemImage: "info.circle")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(Color(UIColor.label))
                    .frame(width: 40, height: 40)
                    .background(photosChromeCircleFill, in: Circle())
                    .shadow(color: .black.opacity(0.12), radius: 4, x: 0, y: 2)
            }
            .accessibilityLabel("更多")
        }
        .padding(.horizontal, AppTheme.Design.spacingM)
        .padding(.bottom, 10)
    }

    private var photosChromeCircleFill: Color {
        Color(UIColor.secondarySystemGroupedBackground)
    }

    private func centerMetaPill(for photo: Photo) -> some View {
        let title = photoTitle(for: photo)
        let subtitle = formattedPhotoDate(photo)

        return VStack(spacing: 2) {
            Text(title)
                .font(.system(size: AppTheme.Design.fontSizeSubheadline, weight: .semibold))
                .foregroundStyle(Color(UIColor.label))
                .lineLimit(1)
                .minimumScaleFactor(0.85)
            Text(subtitle)
                .font(.system(size: AppTheme.Design.fontSizeCaption, weight: .regular))
                .foregroundStyle(Color(UIColor.secondaryLabel))
                .lineLimit(1)
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, 18)
        .padding(.vertical, 10)
        .frame(maxWidth: 260)
        .background {
            Capsule(style: .continuous)
                .fill(.ultraThinMaterial)
                .shadow(color: .black.opacity(0.06), radius: 8, x: 0, y: 2)
        }
        .allowsHitTesting(false)
    }

    // MARK: - Bottom

    private var bottomChrome: some View {
        VStack(spacing: 0) {
            thumbnailStrip
                .padding(.bottom, 10)

            bottomToolbar
                .padding(.top, 6)
                .padding(.bottom, 10)
                .background(.ultraThinMaterial)
        }
    }

    private var thumbnailStrip: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(photos.enumerated()), id: \.element.id) { index, photo in
                        thumbnailCell(photo: photo, index: index, isSelected: index == currentPage)
                            .id(index)
                    }
                }
                .padding(.horizontal, AppTheme.Design.spacingM)
            }
            .frame(height: 64)
            .onChange(of: currentPage) { _, newValue in
                withAnimation(.easeInOut(duration: 0.2)) {
                    proxy.scrollTo(newValue, anchor: .center)
                }
            }
            .onAppear {
                proxy.scrollTo(currentPage, anchor: .center)
            }
        }
    }

    private func thumbnailCell(photo: Photo, index: Int, isSelected: Bool) -> some View {
        let thumbURL = (photo.thumbnailUrl.flatMap { URL(string: String(describing: $0)) })
            ?? URL(string: String(describing: photo.url))

        return Button {
            currentPage = index
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .fill(Color(UIColor.tertiarySystemFill))

                if let url = thumbURL {
                    CachedPhotoImage(url: url, contentMode: .fill, skeletonPlacement: .thumbnailStrip)
                }
            }
            .frame(width: 52, height: 52)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .strokeBorder(
                        isSelected ? Color.accentColor : Color.clear,
                        lineWidth: isSelected ? 3 : 0
                    )
            }
            .scaleEffect(isSelected ? 1.06 : 1)
            .animation(.spring(response: 0.28, dampingFraction: 0.82), value: isSelected)
        }
        .buttonStyle(.plain)
    }

    private var bottomToolbar: some View {
        HStack {
            toolbarItem("square.and.arrow.up", label: "分享") {
                shareCurrentPhoto()
            }
            toolbarItem(
                favoriteIds.contains(currentPhoto?.id ?? "") ? "heart.fill" : "heart",
                label: "收藏"
            ) {
                guard let id = currentPhoto?.id else { return }
                if favoriteIds.contains(id) {
                    favoriteIds.remove(id)
                } else {
                    favoriteIds.insert(id)
                }
            }
            toolbarItem("info.circle", label: "信息") {
                showInfo = true
            }
            toolbarItem("slider.horizontal.3", label: "编辑") {
                showEditPlaceholder = true
            }
            toolbarItem("trash", label: "删除", destructive: true) {
                showDeleteConfirm = true
            }
        }
        .padding(.horizontal, AppTheme.Design.spacingL)
    }

    private func toolbarItem(
        _ systemName: String,
        label: String,
        destructive: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 22))
                .symbolRenderingMode(.monochrome)
                .foregroundStyle(destructive ? Color(UIColor.systemRed) : Color(UIColor.label))
                .frame(maxWidth: .infinity, minHeight: 44)
        }
        .accessibilityLabel(label)
    }

    // MARK: - Copy / dates

    private func photoTitle(for photo: Photo) -> String {
        let name = String(describing: photo.name).trimmingCharacters(in: .whitespacesAndNewlines)
        if name.isEmpty { return "照片" }
        return (name as NSString).deletingPathExtension
    }

    private func formattedPhotoDate(_ photo: Photo) -> String {
        // Kotlin `PhotoTime.kt` → framework `PhotoTimeKt.photoCreatedAtEpochMilliseconds(photo:)` (returns `Int64`).
        let ms = PhotoTimeKt.photoCreatedAtEpochMilliseconds(photo: photo)
        let date = Date(timeIntervalSince1970: TimeInterval(ms) / 1000.0)
        let df = DateFormatter()
        df.locale = Locale.current
        df.dateStyle = .medium
        df.timeStyle = .short
        return df.string(from: date)
    }

    /// Bridges Kotlin `Long` export (`KotlinLong` or `Int64`) to `Int64`.
    private func kotlinLongBits(_ value: Any) -> Int64 {
        if let k = value as? KotlinLong { return k.int64Value }
        if let i = value as? Int64 { return i }
        if let i = value as? Int32 { return Int64(i) }
        return 0
    }

    private func infoMessage(for photo: Photo) -> String {
        let w = Int(photo.width)
        let h = Int(photo.height)
        let bytes = kotlinLongBits(photo.size)
        let mb = Double(bytes) / (1024 * 1024)
        return """
        文件名：\(photo.name)
        尺寸：\(w) × \(h)
        大小：\(String(format: "%.2f MB", mb))
        类型：\(photo.mimeType)
        """
    }

    private func shareCurrentPhoto() {
        guard let photo = currentPhoto,
              let url = URL(string: String(describing: photo.url)) else { return }
        let av = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let root = scene.keyWindow?.rootViewController else { return }
        var top = root
        while let presented = top.presentedViewController { top = presented }
        if let pop = av.popoverPresentationController {
            pop.sourceView = top.view
            pop.sourceRect = CGRect(x: top.view.bounds.midX, y: top.view.bounds.midY, width: 1, height: 1)
            pop.permittedArrowDirections = []
        }
        top.present(av, animated: true)
    }
}

// MARK: - UIWindowScene helper

private extension UIWindowScene {
    var keyWindow: UIWindow? {
        windows.first { $0.isKeyWindow }
    }
}

// MARK: - UIKit 宿主：分页

private struct PhotoFullscreenHostRepresentable: UIViewControllerRepresentable {
    let urlStrings: [String]
    let initialPage: Int
    @Binding var currentPage: Int
    let onSingleTap: () -> Void
    /// `true`：双击放大铺满后隐藏 SwiftUI 顶/底栏；`false`：恢复适配时重新显示。
    let onDoubleTapFillChrome: (Bool) -> Void

    func makeUIViewController(context: Context) -> PhotoFullscreenHostViewController {
        let coord = context.coordinator
        let vc = PhotoFullscreenHostViewController(
            urlStrings: urlStrings,
            initialPage: initialPage,
            onPageChange: { coord.syncPage($0) },
            onSingleTap: onSingleTap,
            onDoubleTapFillChrome: onDoubleTapFillChrome
        )
        coord.host = vc
        coord.currentPageBinding = $currentPage
        coord.lastSyncedPage = initialPage
        return vc
    }

    func updateUIViewController(_ uiViewController: PhotoFullscreenHostViewController, context: Context) {
        context.coordinator.currentPageBinding = $currentPage
        context.coordinator.host = uiViewController
        uiViewController.updateURLsIfNeeded(urlStrings)
        if context.coordinator.lastSyncedPage != currentPage {
            uiViewController.scrollToPage(currentPage, animated: false)
            context.coordinator.lastSyncedPage = currentPage
        }
        uiViewController.onSingleTap = onSingleTap
        uiViewController.onDoubleTapFillChrome = onDoubleTapFillChrome
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator {
        weak var host: PhotoFullscreenHostViewController?
        var currentPageBinding: Binding<Int>?
        var lastSyncedPage: Int = 0

        func syncPage(_ index: Int) {
            lastSyncedPage = index
            currentPageBinding?.wrappedValue = index
        }
    }
}

private final class PhotoFullscreenHostViewController: UIViewController, UIPageViewControllerDataSource, UIPageViewControllerDelegate {
    private let embeddedPageController = UIPageViewController(
        transitionStyle: .scroll,
        navigationOrientation: .horizontal,
        options: [UIPageViewController.OptionsKey.interPageSpacing: 0]
    )

    private var urlStrings: [String]
    private var pageCache: [Int: PhotoPageViewController] = [:]
    private var currentIndex: Int = 0

    var onPageChange: ((Int) -> Void)?
    var onSingleTap: (() -> Void)?
    var onDoubleTapFillChrome: ((Bool) -> Void)?

    init(
        urlStrings: [String],
        initialPage: Int,
        onPageChange: @escaping (Int) -> Void,
        onSingleTap: @escaping () -> Void,
        onDoubleTapFillChrome: @escaping (Bool) -> Void
    ) {
        self.urlStrings = urlStrings
        self.currentIndex = urlStrings.isEmpty ? 0 : min(max(initialPage, 0), urlStrings.count - 1)
        self.onPageChange = onPageChange
        self.onSingleTap = onSingleTap
        self.onDoubleTapFillChrome = onDoubleTapFillChrome
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func updateURLsIfNeeded(_ urls: [String]) {
        guard urls != urlStrings else { return }
        urlStrings = urls
        pageCache.removeAll()
        embeddedPageController.setViewControllers(nil, direction: .forward, animated: false, completion: nil)
        if urls.isEmpty {
            currentIndex = 0
            return
        }
        currentIndex = min(currentIndex, urls.count - 1)
        if let vc = pageController(at: currentIndex) {
            embeddedPageController.setViewControllers([vc], direction: .forward, animated: false, completion: nil)
        }
    }

    func scrollToPage(_ index: Int, animated: Bool) {
        guard index >= 0, index < urlStrings.count, index != currentIndex else { return }
        let dir: UIPageViewController.NavigationDirection = index > currentIndex ? .forward : .reverse
        guard let vc = pageController(at: index) else { return }
        embeddedPageController.setViewControllers([vc], direction: dir, animated: animated) { _ in
            self.currentIndex = index
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let bg = UIColor.systemBackground
        view.backgroundColor = bg

        addChild(embeddedPageController)
        embeddedPageController.dataSource = self
        embeddedPageController.delegate = self
        embeddedPageController.view.backgroundColor = bg
        embeddedPageController.view.frame = view.bounds
        embeddedPageController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(embeddedPageController.view)
        embeddedPageController.didMove(toParent: self)

        if let first = pageController(at: currentIndex) {
            embeddedPageController.setViewControllers([first], direction: .forward, animated: false, completion: nil)
        }
    }

    override var prefersHomeIndicatorAutoHidden: Bool { false }

    override var prefersStatusBarHidden: Bool { false }

    func pageController(at index: Int) -> PhotoPageViewController? {
        guard index >= 0, index < urlStrings.count else { return nil }
        if let c = pageCache[index] {
            return c
        }
        let vc = PhotoPageViewController(pageIndex: index, urlString: urlStrings[index])
        vc.onSingleTap = { [weak self] in self?.onSingleTap?() }
        vc.onDoubleTapFillChrome = { [weak self] hide in self?.onDoubleTapFillChrome?(hide) }
        pageCache[index] = vc
        return vc
    }

    // MARK: UIPageViewControllerDataSource

    func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerBefore viewController: UIViewController
    ) -> UIViewController? {
        guard let vc = viewController as? PhotoPageViewController else { return nil }
        let i = vc.pageIndex
        guard i > 0 else { return nil }
        return pageController(at: i - 1)
    }

    func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerAfter viewController: UIViewController
    ) -> UIViewController? {
        guard let vc = viewController as? PhotoPageViewController else { return nil }
        let i = vc.pageIndex
        guard i + 1 < urlStrings.count else { return nil }
        return pageController(at: i + 1)
    }

    // MARK: UIPageViewControllerDelegate

    func pageViewController(
        _ pageViewController: UIPageViewController,
        didFinishAnimating finished: Bool,
        previousViewControllers: [UIViewController],
        transitionCompleted completed: Bool
    ) {
        guard completed, let vc = pageViewController.viewControllers?.first as? PhotoPageViewController else { return }
        currentIndex = vc.pageIndex
        onPageChange?(currentIndex)
        vc.applyZoomFitAfterPageTransition()
    }
}

// MARK: - 单页：加载 + 缩放壳

private final class PhotoPageViewController: UIViewController {
    let pageIndex: Int
    let urlString: String
    var onSingleTap: (() -> Void)?
    var onDoubleTapFillChrome: ((Bool) -> Void)?

    let zoomShell = PhotoZoomScrollContainerView()
    private let skeletonLoading = UIView()
    private var task: Task<Void, Never>?

    init(pageIndex: Int, urlString: String) {
        self.pageIndex = pageIndex
        self.urlString = urlString
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let bg = UIColor.systemBackground
        view.backgroundColor = bg

        skeletonLoading.translatesAutoresizingMaskIntoConstraints = false
        skeletonLoading.backgroundColor = bg
        skeletonLoading.clipsToBounds = true
        skeletonLoading.isSkeletonable = true
        view.addSubview(skeletonLoading)
        NSLayoutConstraint.activate([
            skeletonLoading.topAnchor.constraint(equalTo: view.topAnchor),
            skeletonLoading.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            skeletonLoading.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            skeletonLoading.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])

        zoomShell.frame = view.bounds
        zoomShell.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        zoomShell.onSingleTap = { [weak self] in self?.onSingleTap?() }
        zoomShell.onDoubleTapFillChrome = { [weak self] hide in self?.onDoubleTapFillChrome?(hide) }
        view.addSubview(zoomShell)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        loadIfNeeded()
    }

    /// 由 `UIPageViewController` 过渡完成后再调用，比 `viewDidAppear` 更贴近「当前页已就位」的时机。
    fileprivate func applyZoomFitAfterPageTransition() {
        DispatchQueue.main.async { [weak self] in
            self?.zoomShell.resetZoomToFit(animated: false)
        }
    }

    private func loadIfNeeded() {
        task?.cancel()
        skeletonLoading.isHidden = false
        showFullScreenSkeletonIfNeeded()
        task = Task {
            guard let url = URL(string: urlString) else {
                await MainActor.run { hideFullScreenSkeleton() }
                return
            }
            do {
                let img = try await PhotoImageCache.shared.image(for: url)
                await MainActor.run {
                    hideFullScreenSkeleton()
                    zoomShell.setImage(img)
                }
            } catch {
                await MainActor.run { hideFullScreenSkeleton() }
            }
        }
    }

    private func showFullScreenSkeletonIfNeeded() {
        skeletonLoading.layoutIfNeeded()
        guard skeletonLoading.bounds.width > 1, skeletonLoading.bounds.height > 1 else { return }
        let color = UIColor.secondarySystemBackground
        let anim = SkeletonPhotoPlaceholder.photoSkeletonSlideAnimation
        if skeletonLoading.sk.isSkeletonActive {
            skeletonLoading.updateAnimatedSkeleton(usingColor: color, animation: anim)
        } else {
            skeletonLoading.showAnimatedSkeleton(usingColor: color, animation: anim, transition: .none)
        }
    }

    private func hideFullScreenSkeleton() {
        skeletonLoading.hideSkeleton(reloadDataAfter: false, transition: .none)
        skeletonLoading.isHidden = true
    }
}

// MARK: - UIScrollView 缩放

private final class PhotoZoomScrollContainerView: UIView, UIScrollViewDelegate {
    fileprivate let scrollView = UIScrollView()
    private let imageView = UIImageView()

    var onSingleTap: (() -> Void)?
    /// 双击放大铺满 / 恢复适配时通知 SwiftUI：`true` 隐藏顶底栏，`false` 显示。
    var onDoubleTapFillChrome: ((Bool) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear

        scrollView.delegate = self
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.bouncesZoom = true
        scrollView.alwaysBounceVertical = true
        scrollView.bounces = true
        scrollView.decelerationRate = .fast
        scrollView.backgroundColor = .clear
        scrollView.contentInsetAdjustmentBehavior = .never

        imageView.isUserInteractionEnabled = true
        imageView.backgroundColor = .clear

        addSubview(scrollView)
        scrollView.addSubview(imageView)

        let doubleTap = UITapGestureRecognizer(target: self, action: #selector(handleDoubleTap(_:)))
        doubleTap.numberOfTapsRequired = 2

        let singleTap = UITapGestureRecognizer(target: self, action: #selector(handleSingleTap))
        singleTap.numberOfTapsRequired = 1
        singleTap.require(toFail: doubleTap)

        scrollView.addGestureRecognizer(doubleTap)
        scrollView.addGestureRecognizer(singleTap)

        // 与「frame = image.size + scrollView 缩放」一致；scaleAspectFit 会在 imageView 内再缩一层，和 contentSize 不一致时容易翻页后显示错位。
        imageView.contentMode = .center
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private var appliedUIImage: UIImage?
    private var laidOutImageSize: CGSize?
    private var lastScrollBoundsForLayout: CGRect?

    func setImage(_ image: UIImage) {
        guard appliedUIImage !== image else { return }
        appliedUIImage = image
        laidOutImageSize = nil
        lastScrollBoundsForLayout = nil
        imageView.image = image
        setNeedsLayout()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        scrollView.frame = bounds

        guard let image = appliedUIImage ?? imageView.image else { return }
        let size = image.size
        let b = scrollView.bounds
        guard b.width > 0, b.height > 0 else { return }

        let boundsMatches = lastScrollBoundsForLayout.map { PhotoZoomScrollContainerView.boundsMatch($0, b) } == true
        let sameImageAndBounds = laidOutImageSize == size && boundsMatches
        if sameImageAndBounds, scrollView.minimumZoomScale > 0 {
            centerImageInScrollView()
            return
        }

        // 必须先回到 zoomScale == 1 再改 imageView.frame / contentSize；否则 UIScrollView 仍带着旧 transform，
        // 直接改 frame 会与缩放矩阵打架，翻页后容易出现「只显示局部、像被放大裁切」且缩不回去。
        scrollView.zoomScale = 1

        laidOutImageSize = size
        lastScrollBoundsForLayout = b
        imageView.image = image
        imageView.frame = CGRect(origin: .zero, size: size)
        scrollView.contentSize = size

        let fit = min(b.width / size.width, b.height / size.height)
        let minScale = fit
        let maxScale = max(minScale * 5, 4)

        scrollView.minimumZoomScale = minScale
        scrollView.maximumZoomScale = maxScale
        scrollView.zoomScale = minScale
        scrollView.contentOffset = .zero
        centerImageInScrollView()
    }

    /// 翻页结束后强制「适配屏幕」缩放，修复过渡期间 layout 造成的错误 zoom。
    fileprivate func resetZoomToFit(animated: Bool = false) {
        guard appliedUIImage != nil || imageView.image != nil else { return }
        scrollView.zoomScale = 1
        setNeedsLayout()
        layoutIfNeeded()
        guard scrollView.minimumZoomScale > 0 else { return }
        scrollView.setZoomScale(scrollView.minimumZoomScale, animated: animated)
        scrollView.contentOffset = .zero
        centerImageInScrollView()
    }

    private static func boundsMatch(_ a: CGRect, _ b: CGRect) -> Bool {
        abs(a.width - b.width) < 0.5 && abs(a.height - b.height) < 0.5
    }

    func viewForZooming(in scrollView: UIScrollView) -> UIView? {
        imageView
    }

    func scrollViewDidZoom(_ scrollView: UIScrollView) {
        centerImageInScrollView()
    }

    private func centerImageInScrollView() {
        let boundsSize = scrollView.bounds.size
        var frame = imageView.frame

        if frame.size.width < boundsSize.width {
            frame.origin.x = (boundsSize.width - frame.size.width) * 0.5
        } else {
            frame.origin.x = 0
        }
        if frame.size.height < boundsSize.height {
            frame.origin.y = (boundsSize.height - frame.size.height) * 0.5
        } else {
            frame.origin.y = 0
        }
        imageView.frame = frame
    }

    @objc private func handleSingleTap() {
        onSingleTap?()
    }

    @objc private func handleDoubleTap(_ gesture: UITapGestureRecognizer) {
        guard let img = appliedUIImage ?? imageView.image else { return }

        if scrollView.zoomScale > scrollView.minimumZoomScale * 1.02 {
            scrollView.setZoomScale(scrollView.minimumZoomScale, animated: true)
            onDoubleTapFillChrome?(false)
            return
        }

        // 以图幅铺满可视区域（aspect fill）：scale = max(bw/iw, bh/ih)，相对系统「适配」的 minScale 通常更大。
        let iw = max(img.size.width, 1)
        let ih = max(img.size.height, 1)
        let bw = scrollView.bounds.width
        let bh = scrollView.bounds.height
        guard bw > 1, bh > 1 else { return }

        var targetScale = max(bw / iw, bh / ih)
        targetScale = min(targetScale, scrollView.maximumZoomScale)
        targetScale = max(targetScale, scrollView.minimumZoomScale)

        let point = gesture.location(in: imageView)
        var rw = bw / targetScale
        var rh = bh / targetScale
        rw = min(rw, iw)
        rh = min(rh, ih)

        var ox = point.x - rw * 0.5
        var oy = point.y - rh * 0.5
        ox = min(max(0, ox), iw - rw)
        oy = min(max(0, oy), ih - rh)
        let rect = CGRect(x: ox, y: oy, width: rw, height: rh)

        scrollView.zoom(to: rect, animated: true)
        onDoubleTapFillChrome?(true)
    }
}
