import SwiftUI
import Shared

/**
 * 拍照视图 - 点击拍照 Tab 直接全屏打开相机，关闭后返回上一个 Tab，不显示占位页
 */
struct CameraView: View {
    @ObservedObject var viewModel: AppViewModel
    var onCameraDismissed: () -> Void
    @State private var showImagePicker = false
    @State private var selectedImage: UIImage?
    @State private var showUploadDialog = false

    var body: some View {
        Color.clear
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .toolbar(.visible, for: .tabBar)
            .onAppear {
                DispatchQueue.main.async {
                    showImagePicker = true
                }
            }
            .fullScreenCover(isPresented: $showImagePicker, onDismiss: {
                if !showUploadDialog {
                    onCameraDismissed()
                }
            }) {
                ImagePicker(
                    image: $selectedImage,
                    showUploadDialog: $showUploadDialog,
                    onCancelCamera: { onCameraDismissed() }
                )
                .ignoresSafeArea(.all)
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
                    selectedImage = nil
                    onCameraDismissed()
                }
                Button("取消", role: .cancel) {
                    selectedImage = nil
                    onCameraDismissed()
                }
            } message: {
                Text("是否上传这张照片到云端？")
            }
    }
}
