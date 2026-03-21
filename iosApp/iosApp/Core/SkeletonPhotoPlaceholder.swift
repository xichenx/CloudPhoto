import SkeletonView
import SwiftUI
import UIKit

/// 使用 [SkeletonView](https://github.com/Juanpe/SkeletonView) 的 **Solid + Animated** 占位；滑动方向 **对角**。
struct SkeletonPhotoPlaceholder: UIViewRepresentable {
    enum Placement: Hashable {
        case gridCell
        case thumbnailStrip
        case fullScreen
    }

    var placement: Placement = .gridCell

    /// 与全屏大图骨架共用：**左上 → 右下** 对角滑动。
    static var photoSkeletonSlideAnimation: SkeletonLayerAnimation {
        GradientDirection.topLeftBottomRight.slidingAnimation(duration: 1.45, autoreverses: false)
    }

    func makeUIView(context: Context) -> UIView {
        let v = UIView()
        v.clipsToBounds = true
        v.layer.cornerRadius = 2
        applyBaseColors(to: v)
        v.isSkeletonable = true
        v.skeletonCornerRadius = 2
        return v
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        applyBaseColors(to: uiView)
        let color = skeletonColor(for: placement)
        let anim = Self.photoSkeletonSlideAnimation
        DispatchQueue.main.async {
            uiView.layoutIfNeeded()
            guard uiView.bounds.width > 1, uiView.bounds.height > 1 else { return }
            if uiView.sk.isSkeletonActive {
                uiView.updateAnimatedSkeleton(usingColor: color, animation: anim)
            } else {
                uiView.showAnimatedSkeleton(usingColor: color, animation: anim, transition: .none)
            }
        }
    }

    static func dismantleUIView(_ uiView: UIView, coordinator: ()) {
        uiView.hideSkeleton(reloadDataAfter: false, transition: .none)
    }

    private func applyBaseColors(to v: UIView) {
        switch placement {
        case .gridCell:
            v.backgroundColor = UIColor.secondarySystemBackground
        case .thumbnailStrip:
            v.backgroundColor = UIColor.tertiarySystemFill
        case .fullScreen:
            v.backgroundColor = UIColor.systemBackground
        }
    }

    private func skeletonColor(for placement: Placement) -> UIColor {
        switch placement {
        case .gridCell:
            return UIColor.tertiarySystemFill
        case .thumbnailStrip:
            return UIColor.quaternarySystemFill
        case .fullScreen:
            return UIColor.secondarySystemBackground
        }
    }
}
