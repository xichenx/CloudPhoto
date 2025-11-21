import SwiftUI

/**
 * 相册视图 - 遵循 Apple HIG 设计规范
 */
struct AlbumsView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: AppTheme.Design.spacingM) {
                Image(systemName: "photo.on.rectangle")
                    .font(.system(size: 64))
                    .foregroundColor(AppTheme.Colors.secondaryText)
                Text("相册功能开发中...")
                    .font(.system(size: AppTheme.Design.fontSizeBody))
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppTheme.Colors.background)
            .navigationTitle("相册")
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        }
        .toolbar(.visible, for: .tabBar)
    }
}
