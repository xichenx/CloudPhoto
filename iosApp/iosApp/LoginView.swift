import SwiftUI
import Shared

/// 登录界面 - 与 Android 登录流程一致，遵循 Apple HIG
struct LoginView: View {
    @ObservedObject var viewModel: AppViewModel
    var onNavigateToRegister: () -> Void
    
    @State private var account = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var passwordVisible = false
    @State private var accountError: String?
    @State private var passwordError: String?
    @FocusState private var focusedField: Field?
    
    private enum Field {
        case account, password
    }
    
    var body: some View {
        ZStack {
            // 渐变背景
            LinearGradient(
                colors: [
                    AppTheme.Colors.primary.opacity(0.15),
                    AppTheme.Colors.background,
                    AppTheme.Colors.background
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 0) {
                    Spacer()
                        .frame(height: 40)
                    
                    // Logo 与标题
                    VStack(spacing: 20) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 20)
                                .fill(AppTheme.Colors.surface)
                                .frame(width: 88, height: 88)
                                .shadow(color: AppTheme.Colors.primary.opacity(0.2), radius: 8, x: 0, y: 4)
                            Image(systemName: "photo.fill")
                                .font(.system(size: 40))
                                .foregroundColor(AppTheme.Colors.primary)
                        }
                        
                        VStack(spacing: 6) {
                            Text("欢迎回来")
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(AppTheme.Colors.text)
                            Text("登录以继续使用云相册")
                                .font(.system(size: AppTheme.Design.fontSizeBody))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(.horizontal)
                    
                    Spacer().frame(height: 36)
                    
                    // 表单卡片
                    VStack(alignment: .leading, spacing: 18) {
                        AuthTextField(
                            label: "邮箱",
                            text: $account,
                            error: $accountError,
                            icon: "envelope",
                            keyboardType: .emailAddress,
                            textContentType: .emailAddress,
                            submitLabel: .next,
                            onSubmit: { focusedField = .password }
                        )
                        .focused($focusedField, equals: .account)
                        
                        AuthTextField(
                            label: "密码",
                            text: $password,
                            error: $passwordError,
                            icon: "lock",
                            isSecure: !passwordVisible,
                            submitLabel: .done,
                            trailing: {
                                Button(action: { passwordVisible.toggle() }) {
                                    Image(systemName: passwordVisible ? "eye.slash" : "eye")
                                        .foregroundColor(AppTheme.Colors.secondaryText)
                                }
                            },
                            onSubmit: { focusedField = nil }
                        )
                        .focused($focusedField, equals: .password)
                        
                        HStack {
                            Spacer()
                            Button("忘记密码？") {
                                // TODO: 找回密码
                            }
                            .font(.system(size: AppTheme.Design.fontSizeFootnote))
                            .foregroundColor(AppTheme.Colors.primary)
                        }
                        
                        Button(action: performLogin) {
                            Group {
                                if isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.9)
                                } else {
                                    Text("登录")
                                        .font(.system(size: AppTheme.Design.fontSizeTitle3, weight: .bold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .foregroundColor(.white)
                            .background(AppTheme.Colors.primary)
                            .cornerRadius(AppTheme.Design.cornerRadiusLarge)
                        }
                        .disabled(isLoading)
                        .buttonStyle(.plain)
                        
                        // 分隔线
                        HStack {
                            Rectangle().fill(AppTheme.Colors.secondaryText.opacity(0.25)).frame(height: 1)
                            Text("或")
                                .font(.system(size: AppTheme.Design.fontSizeFootnote))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                            Rectangle().fill(AppTheme.Colors.secondaryText.opacity(0.25)).frame(height: 1)
                        }
                        
                        Button(action: {
                            viewModel.mockLogin()
                        }) {
                            HStack(spacing: 8) {
                                Image(systemName: "bolt.fill")
                                    .font(.system(size: 18))
                                Text("快速登录（Mock）")
                                    .font(.system(size: AppTheme.Design.fontSizeBody, weight: .medium))
                            }
                            .foregroundColor(AppTheme.Colors.secondary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .overlay(
                                RoundedRectangle(cornerRadius: AppTheme.Design.cornerRadiusLarge)
                                    .stroke(AppTheme.Colors.secondary.opacity(0.6), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                        
                        HStack(spacing: 4) {
                            Text("还没有账户？")
                                .font(.system(size: AppTheme.Design.fontSizeBody))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                            Button("立即注册") {
                                viewModel.trackLoginGoRegister()
                                onNavigateToRegister()
                            }
                            .font(.system(size: AppTheme.Design.fontSizeBody, weight: .bold))
                            .foregroundColor(AppTheme.Colors.primary)
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .padding(AppTheme.Design.spacingL)
                    .background(AppTheme.Colors.surface)
                    .cornerRadius(AppTheme.Design.cornerRadiusXLarge)
                    .shadow(color: AppTheme.Colors.primary.opacity(0.12), radius: 8, x: 0, y: 4)
                    .padding(.horizontal, 20)
                    
                    Spacer().frame(height: 60)
                }
            }
        }
        .onTapGesture { focusedField = nil }
        .onChange(of: viewModel.authError) { _, newValue in
            if newValue != nil {
                isLoading = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    viewModel.clearAuthError()
                }
            }
        }
        .onChange(of: viewModel.isLoggedIn) { _, loggedIn in
            if loggedIn { isLoading = false }
        }
        .toast(
            message: Binding(
                get: { viewModel.authError },
                set: { _ in viewModel.clearAuthError() }
            ),
            type: .error
        )
    }
    
    private func performLogin() {
        accountError = nil
        passwordError = nil
        focusedField = nil
        
        var hasError = false
        let trimmedAccount = account.trimmingCharacters(in: .whitespaces)
        if trimmedAccount.isEmpty {
            accountError = "请输入邮箱"
            hasError = true
        } else {
            let emailPredicate = NSPredicate(format: "SELF MATCHES %@", "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
            if !emailPredicate.evaluate(with: trimmedAccount) {
                accountError = "请输入有效的邮箱地址"
                hasError = true
            }
        }
        if password.isEmpty {
            passwordError = "请输入密码"
            hasError = true
        }
        if hasError { return }
        
        isLoading = true
        viewModel.trackLoginSubmit()
        viewModel.login(account: trimmedAccount, password: password)
    }
}
