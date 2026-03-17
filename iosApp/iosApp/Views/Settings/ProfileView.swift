import SwiftUI
import Shared

/**
 * 个人资料页 - 展示并可编辑用户信息（如用户名），遵循 Apple HIG 设计规范
 */
struct ProfileView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var showEditSheet = false
    @State private var toastMessage: String?

    private var user: UserDTO? {
        viewModel.currentUser
    }

    private var avatarLetter: String {
        guard let name = user?.username, !name.isEmpty else { return "?" }
        return String(name.prefix(1)).uppercased()
    }

    private func formatCreatedAt(_ epochSeconds: KotlinLong?) -> String {
        guard let seconds = epochSeconds?.int64Value else { return "—" }
        let date = Date(timeIntervalSince1970: TimeInterval(seconds))
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        formatter.locale = Locale.current
        return formatter.string(from: date)
    }

    var body: some View {
        List {
            Section {
                VStack(spacing: AppTheme.Design.spacingM) {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [AppTheme.Colors.primary, AppTheme.Colors.tertiary],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 96, height: 96)
                            .shadow(color: AppTheme.Colors.primary.opacity(0.3), radius: 8, x: 0, y: 4)

                        Text(avatarLetter)
                            .font(.system(size: 40, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .padding(.top, AppTheme.Design.spacingS)

                    Text(user?.username ?? "未登录")
                        .font(.system(size: AppTheme.Design.fontSizeTitle3, weight: .semibold))
                        .foregroundColor(AppTheme.Colors.text)
                }
                .frame(maxWidth: .infinity)
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets(top: AppTheme.Design.spacingM, leading: 0, bottom: AppTheme.Design.spacingM, trailing: 0))
            }

            Section("账户信息") {
                ProfileRow(label: "用户名", value: user?.username ?? "—", icon: "person.fill")
                ProfileRow(label: "邮箱", value: (user?.email).flatMap { $0.isEmpty ? nil : $0 } ?? "—", icon: "envelope.fill")
                ProfileRow(label: "手机号", value: (user?.phone).flatMap { $0.isEmpty ? nil : $0 } ?? "—", icon: "phone.fill")
                ProfileRow(label: "用户 ID", value: user?.id ?? "—", icon: "number")
                ProfileRow(label: "注册时间", value: formatCreatedAt(user?.createdAt), icon: "calendar")
            }
        }
        .navigationTitle("个人资料")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("编辑") {
                    showEditSheet = true
                }
                .foregroundColor(AppTheme.Colors.primary)
            }
        }
        .sheet(isPresented: $showEditSheet) {
            EditProfileSheet(
                currentUsername: user?.username ?? "",
                onDismiss: { showEditSheet = false },
                onSave: { newUsername in
                    viewModel.updateProfile(username: newUsername.isEmpty ? nil : newUsername)
                    showEditSheet = false
                }
            )
        }
        .onChange(of: viewModel.authError) { _, newValue in
            if let msg = newValue {
                toastMessage = msg
                viewModel.clearAuthError()
            }
        }
        .toast(message: $toastMessage, type: .error)
    }
}

/**
 * 编辑资料 Sheet - 用户名等可编辑项
 */
private struct EditProfileSheet: View {
    let currentUsername: String
    let onDismiss: () -> Void
    let onSave: (String) -> Void
    @State private var username: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section("用户名") {
                    TextField("请输入用户名", text: $username)
                        .autocapitalization(.none)
                }
            }
            .navigationTitle("编辑资料")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear { username = currentUsername }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        onDismiss()
                    }
                    .foregroundColor(AppTheme.Colors.primary)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("保存") {
                        onSave(username)
                    }
                    .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }
}

/**
 * 个人资料行 - 标签 + 值，带图标
 */
private struct ProfileRow: View {
    let label: String
    let value: String
    let icon: String

    var body: some View {
        HStack(spacing: AppTheme.Design.spacingM) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(AppTheme.Colors.primary)
                .frame(width: 24, alignment: .center)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: AppTheme.Design.fontSizeFootnote))
                    .foregroundColor(AppTheme.Colors.secondaryText)
                Text(value)
                    .font(.system(size: AppTheme.Design.fontSizeBody))
                    .foregroundColor(AppTheme.Colors.text)
                    .lineLimit(2)
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 4)
    }
}

#if DEBUG
struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ProfileView(viewModel: AppViewModel())
        }
    }
}
#endif
