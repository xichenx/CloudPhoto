import SwiftUI

/**
 * 主题设置页 - 跟随系统 / 浅色 / 深色
 * 使用与 ThemedView 相同的 @AppStorage("themeMode")，取值 "system" / "light" / "dark"
 */
struct ThemeSettingsView: View {
    @AppStorage("themeMode") private var themeMode: String = "system"

    private let options: [(id: String, title: String, subtitle: String, icon: String)] = [
        ("system", "跟随系统", "根据系统深色/浅色模式自动切换", "gearshape"),
        ("light", "浅色", "始终使用浅色主题", "sun.max"),
        ("dark", "深色", "始终使用深色主题", "moon")
    ]

    var body: some View {
        List {
            Section {
                ForEach(options, id: \.id) { option in
                    ThemeOptionRow(
                        title: option.title,
                        subtitle: option.subtitle,
                        icon: option.icon,
                        selected: themeMode == option.id,
                        action: { themeMode = option.id }
                    )
                }
            } header: {
                Text("外观")
            } footer: {
                Text("更改后立即生效")
            }
        }
        .navigationTitle("主题设置")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct ThemeOptionRow: View {
    let title: String
    let subtitle: String
    let icon: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: AppTheme.Design.spacingM) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(selected ? AppTheme.Colors.primary : AppTheme.Colors.secondaryText)
                    .frame(width: 28, alignment: .center)

                VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                    Text(title)
                        .font(.system(size: AppTheme.Design.fontSizeBody, weight: .medium))
                        .foregroundColor(AppTheme.Colors.text)
                    Text(subtitle)
                        .font(.system(size: AppTheme.Design.fontSizeFootnote))
                        .foregroundColor(AppTheme.Colors.secondaryText)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                if selected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
            .padding(.vertical, AppTheme.Design.spacingXS)
            .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    NavigationStack {
        ThemeSettingsView()
    }
}
