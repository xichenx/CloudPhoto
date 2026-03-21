import SwiftUI
import Shared

/**
 * 照片视图 - 遵循 Apple HIG 设计规范
 */
struct PhotosView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var showImagePicker = false
    @State private var selectedImage: UIImage?
    @State private var showUploadDialog = false
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
                onDismiss: { showFullscreenGallery = false },
                onDeletePhoto: { photoId in
                    viewModel.deletePhoto(photoId: photoId)
                }
            )
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: $selectedImage, showUploadDialog: $showUploadDialog)
        }
        .alert("上传照片", isPresented: $showUploadDialog) {
            Button("上传") {
                if let image = selectedImage,
                   let imageData = image.jpegData(compressionQuality: 0.8) {
                    viewModel.uploadPhoto(
                        photoData: imageData,
                        fileName: "photo_\(Date().timeIntervalSince1970).jpg",
                        mimeType: "image/jpeg",
                        width: Int32(image.size.width),
                        height: Int32(image.size.height)
                    )
                }
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("是否上传这张照片到云端？")
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
                Button(action: { showImagePicker = true }) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(AppTheme.Colors.primary)
                }
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
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showImagePicker = true }) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }
}
