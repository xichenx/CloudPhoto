import SwiftUI
import Shared

/**
 * 照片视图 - 遵循 Apple HIG 设计规范
 */
struct PhotosView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var showFullscreenGallery = false
    @State private var fullscreenStartIndex = 0

    var body: some View {
        NavigationView {
            if viewModel.photos.isEmpty {
                photosEmptyContent
            } else {
                photosGridContent
            }
        }
        .toolbar(.visible, for: .tabBar)
        .fullScreenCover(isPresented: $showFullscreenGallery) {
            PhotoFullscreenViewer(
                photos: viewModel.photos,
                initialIndex: fullscreenStartIndex,
                onDismiss: {
                    viewModel.trackPhotoFullscreenClose()
                    showFullscreenGallery = false
                },
                onDeletePhoto: { photoId in
                    viewModel.deletePhoto(photoId: photoId)
                }
            )
        }
    }

    private var photosEmptyContent: some View {
        VStack(spacing: AppTheme.Design.spacingM) {
            Image(systemName: "photo")
                .font(.system(size: 64))
                .foregroundColor(AppTheme.Colors.secondaryText)
            Text("还没有照片")
                .font(.system(size: AppTheme.Design.fontSizeTitle2, weight: .semibold))
                .foregroundColor(AppTheme.Colors.text)
            Text("去拍照 Tab 拍摄第一张照片吧")
                .font(.system(size: AppTheme.Design.fontSizeBody))
                .foregroundColor(AppTheme.Colors.secondaryText)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.Colors.background)
        .navigationTitle("照片")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.trackPhotoSearchTap()
                }) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(AppTheme.Colors.primary)
                }
                .accessibilityLabel("搜索")
            }
        }
    }

    private var photosGridContent: some View {
        ScrollView {
            LazyVGrid(
                columns: [
                    GridItem(.flexible(), spacing: 1),
                    GridItem(.flexible(), spacing: 1),
                    GridItem(.flexible(), spacing: 1)
                ],
                spacing: 1
            ) {
                ForEach(Array(viewModel.photos.enumerated()), id: \.element.id) { index, photo in
                    CachedPhotoImage(
                        url: photo.thumbnailUrl.flatMap { URL(string: String(describing: $0)) }
                            ?? URL(string: String(describing: photo.url)),
                        contentMode: .fill
                    )
                        .frame(width: UIScreen.main.bounds.width / 3 - 1, height: UIScreen.main.bounds.width / 3 - 1)
                        .background(AppTheme.Colors.surface)
                        .photoGridItemStyle()
                        .onTapGesture {
                            viewModel.trackPhotoGridItem(photo: photo, positionOneBased: index + 1)
                            fullscreenStartIndex = index
                            showFullscreenGallery = true
                        }
                }
            }
            .padding(0)
        }
        .background(AppTheme.Colors.background)
        .navigationTitle("照片")
        .navigationBarTitleDisplayMode(.large)
        // 导航栏区域透明：滚到顶栏下的照片不被叠色/毛玻璃影响；标题仍用系统默认 label 色，与其它页一致
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.trackPhotoSearchTap()
                }) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(AppTheme.Colors.primary)
                }
                .accessibilityLabel("搜索")
            }
        }
    }
}
