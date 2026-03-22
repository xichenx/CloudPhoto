import SwiftUI
import Shared

/**
 * 设置视图 - 遵循 Apple HIG 设计规范
 * 使用 NavigationStack + path：仅在根界面显示 Tab 栏，二级界面（个人资料、账号安全、修改密码）隐藏 Tab 栏，与存储页添加配置实现一致。
 */
struct SettingsView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var path: [SettingsRoute] = []

    private var userAvatarLetter: String {
        guard let name = viewModel.currentUser?.username, !name.isEmpty else { return "?" }
        return String(name.prefix(1))
    }

    var body: some View {
        NavigationStack(path: $path) {
            List {
                Section {
                    HStack(spacing: AppTheme.Design.spacingM) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [AppTheme.Colors.primary, AppTheme.Colors.tertiary],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 72, height: 72)
                                .shadow(color: AppTheme.Colors.primary.opacity(0.3), radius: 8, x: 0, y: 4)

                            Text(userAvatarLetter)
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(.white)
                        }

                        VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                            Text(viewModel.currentUser?.username ?? "用户")
                                .font(.system(size: AppTheme.Design.fontSizeTitle3, weight: .semibold))
                                .foregroundColor(AppTheme.Colors.text)
                            Text(viewModel.currentUser?.email ?? "登录后显示详细信息")
                                .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                        }

                        Spacer()
                    }
                    .padding(.vertical, AppTheme.Design.spacingXS)

                    Button(role: .destructive, action: { viewModel.logout() }) {
                        Label("退出登录", systemImage: "rectangle.portrait.and.arrow.right")
                            .frame(maxWidth: .infinity)
                    }
                }

                Section("账户") {
                    NavigationLink(value: SettingsRoute.profile) {
                        Label("个人资料", systemImage: "person")
                    }
                    NavigationLink(value: SettingsRoute.accountSecurity) {
                        Label("账户安全", systemImage: "lock.shield")
                    }
                }

                Section("应用") {
                    NavigationLink(value: SettingsRoute.themeSettings) {
                        Label("主题设置", systemImage: "paintpalette")
                    }
                    NavigationLink(destination: EmptyView()) {
                        Label("通知设置", systemImage: "bell")
                    }
                }

                Section("其他") {
                    NavigationLink(destination: EmptyView()) {
                        Label("帮助与反馈", systemImage: "questionmark.circle")
                    }
                    NavigationLink(destination: EmptyView()) {
                        Label("关于", systemImage: "info.circle")
                    }
                }
            }
            .navigationTitle("我的")
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
            .navigationDestination(for: SettingsRoute.self) { route in
                switch route {
                case .profile:
                    ProfileView(viewModel: viewModel)
                case .accountSecurity:
                    AccountSecurityView(viewModel: viewModel)
                case .changePassword:
                    ChangePasswordView(viewModel: viewModel)
                case .themeSettings:
                    ThemeSettingsView()
                }
            }
        }
        .toolbar(path.isEmpty ? .visible : .hidden, for: .tabBar)
    }
}
