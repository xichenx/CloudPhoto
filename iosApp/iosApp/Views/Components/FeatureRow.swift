import SwiftUI

/// 功能说明行 - 图标 + 文案，用于设置等列表
struct FeatureRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: AppTheme.Design.spacingM) {
            Image(systemName: icon)
                .foregroundColor(AppTheme.Colors.primary)
                .frame(width: 20)
            Text(text)
                .font(.system(size: AppTheme.Design.fontSizeBody))
                .foregroundColor(AppTheme.Colors.text)
        }
    }
}
