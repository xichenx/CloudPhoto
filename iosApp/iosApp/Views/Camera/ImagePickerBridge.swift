import SwiftUI
import UIKit

/// 全屏相机：用容器 VC 包一层，使相机视图铺满整个屏幕（含安全区域外）
final class FullScreenCameraContainerViewController: UIViewController {
    private let picker = UIImagePickerController()
    private var onImagePicked: ((UIImage) -> Void)?
    private var onCancel: (() -> Void)?

    func configure(onImagePicked: @escaping (UIImage) -> Void, onCancel: @escaping () -> Void) {
        self.onImagePicked = onImagePicked
        self.onCancel = onCancel
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black

        picker.delegate = self
        picker.sourceType = .camera
        picker.cameraCaptureMode = .photo
        picker.modalPresentationStyle = .overFullScreen

        addChild(picker)
        picker.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(picker.view)
        NSLayoutConstraint.activate([
            picker.view.topAnchor.constraint(equalTo: view.topAnchor),
            picker.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            picker.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            picker.view.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        picker.didMove(toParent: self)
    }
}

extension FullScreenCameraContainerViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        if let image = info[.originalImage] as? UIImage {
            DispatchQueue.main.async { [weak self] in
                self?.onImagePicked?(image)
            }
        }
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        DispatchQueue.main.async { [weak self] in
            self?.onCancel?()
        }
    }
}

/// SwiftUI 封装的系统相机 / 相册选择
struct ImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Binding var showUploadDialog: Bool
    /// 用户点击取消关闭相机时先执行（如先切回上一 Tab），再 dismiss，避免先露出空白占位
    var onCancelCamera: (() -> Void)? = nil
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> FullScreenCameraContainerViewController {
        let container = FullScreenCameraContainerViewController()
        let onCancelCamera = onCancelCamera
        container.configure(
            onImagePicked: { img in
                image = img
                showUploadDialog = true
                dismiss()
            },
            onCancel: {
                onCancelCamera?()
                dismiss()
            }
        )
        return container
    }

    func updateUIViewController(_ uiViewController: FullScreenCameraContainerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject {
        let parent: ImagePicker
        init(_ parent: ImagePicker) { self.parent = parent }
    }
}
