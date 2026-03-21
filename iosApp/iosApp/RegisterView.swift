import SwiftUI
import Shared

/// 注册界面 - 与 Android 注册流程一致，遵循 Apple HIG
struct RegisterView: View {
    @ObservedObject var viewModel: AppViewModel
    var onNavigateToLogin: () -> Void
    
    @State private var name = ""
    @State private var email = ""
    @State private var emailCode = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var isSendingCode = false
    @State private var countdown = 0
    @State private var codeSentHint = false
    @State private var passwordVisible = false
    @State private var confirmPasswordVisible = false
    @State private var nameError: String?
    @State private var emailError: String?
    @State private var emailCodeError: String?
    @State private var passwordError: String?
    @State private var confirmPasswordError: String?
    
    var body: some View {
        ZStack {
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
                    Spacer().frame(height: 32)
                    
                    VStack(spacing: 20) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 20)
                                .fill(AppTheme.Colors.surface)
                                .frame(width: 88, height: 88)
                                .shadow(color: AppTheme.Colors.primary.opacity(0.2), radius: 8, x: 0, y: 4)
                            Image(systemName: "person.crop.circle.badge.plus")
                                .font(.system(size: 40))
                                .foregroundColor(AppTheme.Colors.primary)
                        }
                        VStack(spacing: 6) {
                            Text("创建账户")
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(AppTheme.Colors.text)
                            Text("注册新账户开始使用")
                                .font(.system(size: AppTheme.Design.fontSizeBody))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(.horizontal)
                    
                    Spacer().frame(height: 28)
                    
                    VStack(alignment: .leading, spacing: 16) {
                        AuthTextField(
                            label: "姓名",
                            text: $name,
                            error: $nameError,
                            icon: "person",
                            submitLabel: .next,
                            onSubmit: {}
                        )
                        
                        AuthTextField(
                            label: "邮箱",
                            text: $email,
                            error: $emailError,
                            icon: "envelope",
                            keyboardType: .emailAddress,
                            textContentType: .emailAddress,
                            submitLabel: .next,
                            onSubmit: {}
                        )
                        
                        // 邮箱验证码行
                        HStack(alignment: .top, spacing: 12) {
                            AuthTextField(
                                label: "邮箱验证码",
                                text: $emailCode,
                                error: $emailCodeError,
                                icon: "lock.shield",
                                keyboardType: .numberPad,
                                submitLabel: .next,
                                onSubmit: {}
                            )
                            
                            if codeSentHint {
                                HStack(spacing: 6) {
                                    Image(systemName: "checkmark.circle")
                                        .foregroundColor(AppTheme.Colors.primary)
                                    Text("已发送")
                                        .font(.system(size: AppTheme.Design.fontSizeFootnote))
                                        .foregroundColor(AppTheme.Colors.primary)
                                }
                                .frame(height: 52)
                                .padding(.horizontal, 12)
                                .background(AppTheme.Colors.primary.opacity(0.12))
                                .cornerRadius(AppTheme.Design.cornerRadiusLarge)
                            } else if countdown > 0 {
                                Text("\(countdown)秒")
                                    .font(.system(size: AppTheme.Design.fontSizeFootnote))
                                    .foregroundColor(AppTheme.Colors.secondaryText)
                                    .frame(width: 70, height: 52)
                            } else {
                                Button(action: sendCode) {
                                    Group {
                                        if isSendingCode {
                                            ProgressView()
                                                .progressViewStyle(CircularProgressViewStyle(tint: AppTheme.Colors.primary))
                                                .scaleEffect(0.8)
                                        } else {
                                            HStack(spacing: 6) {
                                                Image(systemName: "envelope")
                                                    .font(.system(size: 16))
                                                Text("获取验证码")
                                                    .font(.system(size: AppTheme.Design.fontSizeFootnote, weight: .medium))
                                            }
                                            .foregroundColor(AppTheme.Colors.primary)
                                        }
                                    }
                                    .frame(minWidth: 100)
                                    .frame(height: 52)
                                }
                                .disabled(isSendingCode || email.trimmingCharacters(in: .whitespaces).isEmpty)
                                .buttonStyle(.plain)
                            }
                        }
                        
                        AuthTextField(
                            label: "密码",
                            text: $password,
                            error: $passwordError,
                            icon: "lock",
                            isSecure: !passwordVisible,
                            submitLabel: .next,
                            trailing: {
                                Button(action: { passwordVisible.toggle() }) {
                                    Image(systemName: passwordVisible ? "eye.slash" : "eye")
                                        .foregroundColor(AppTheme.Colors.secondaryText)
                                }
                            },
                            onSubmit: {}
                        )
                        
                        AuthTextField(
                            label: "确认密码",
                            text: $confirmPassword,
                            error: $confirmPasswordError,
                            icon: "lock",
                            isSecure: !confirmPasswordVisible,
                            submitLabel: .done,
                            trailing: {
                                Button(action: { confirmPasswordVisible.toggle() }) {
                                    Image(systemName: confirmPasswordVisible ? "eye.slash" : "eye")
                                        .foregroundColor(AppTheme.Colors.secondaryText)
                                }
                            },
                            onSubmit: { performRegister() }
                        )
                        
                        Button(action: performRegister) {
                            Group {
                                if isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.9)
                                } else {
                                    Text("注册")
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
                        
                        HStack(spacing: 4) {
                            Text("已有账户？")
                                .font(.system(size: AppTheme.Design.fontSizeBody))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                            Button("立即登录") {
                                onNavigateToLogin()
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
        .onChange(of: viewModel.authError) { _, newValue in
            if newValue != nil {
                isLoading = false
                isSendingCode = false
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
    
    private func sendCode() {
        emailError = nil
        let trimmed = email.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            emailError = "请先输入邮箱"
            return
        }
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
        if !emailPredicate.evaluate(with: trimmed) {
            emailError = "邮箱格式不正确"
            return
        }
        if countdown > 0 { return }
        
        isSendingCode = true
        viewModel.sendEmailCode(email: trimmed, type: "register")
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            isSendingCode = false
            codeSentHint = true
            countdown = 60
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                codeSentHint = false
            }
            Task { @MainActor in
                for _ in 0..<60 {
                    try? await Task.sleep(nanoseconds: 1_000_000_000)
                    if countdown > 0 { countdown -= 1 }
                }
            }
        }
    }
    
    private func performRegister() {
        nameError = nil
        emailError = nil
        emailCodeError = nil
        passwordError = nil
        confirmPasswordError = nil
        
        var hasError = false
        if name.trimmingCharacters(in: .whitespaces).isEmpty {
            nameError = "请输入姓名"
            hasError = true
        }
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)
        if trimmedEmail.isEmpty {
            emailError = "请输入邮箱"
            hasError = true
        } else {
            let emailPredicate = NSPredicate(format: "SELF MATCHES %@", "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
            if !emailPredicate.evaluate(with: trimmedEmail) {
                emailError = "邮箱格式不正确"
                hasError = true
            }
        }
        if emailCode.isEmpty {
            emailCodeError = "请输入邮箱验证码"
            hasError = true
        }
        if password.isEmpty {
            passwordError = "请输入密码"
            hasError = true
        } else if password.count < 6 {
            passwordError = "密码长度至少6位"
            hasError = true
        }
        if confirmPassword.isEmpty {
            confirmPasswordError = "请确认密码"
            hasError = true
        } else if password != confirmPassword {
            confirmPasswordError = "两次输入的密码不一致"
            hasError = true
        }
        if hasError { return }
        
        isLoading = true
        viewModel.register(
            username: name.trimmingCharacters(in: .whitespaces),
            email: trimmedEmail,
            password: password,
            emailCode: emailCode
        )
    }
}
