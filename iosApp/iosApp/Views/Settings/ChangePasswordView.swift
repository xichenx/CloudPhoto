import SwiftUI
import Shared

/**
 * 修改密码独立界面：先验证码验证，再输入当前密码与新密码修改，遵循 Apple HIG 设计规范
 */
struct ChangePasswordView: View {
    @ObservedObject var viewModel: AppViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var step = 1
    @State private var email = ""
    @State private var emailCode = ""
    @State private var countdown = 0
    @State private var oldPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var toastMessage: String?
    @State private var showSuccessAlert = false

    var body: some View {
        Group {
            if step == 1 {
                verifyStepContent
            } else {
                passwordStepContent
            }
        }
        .navigationTitle(step == 1 ? "验证身份" : "设置新密码")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if let userEmail = viewModel.currentUser?.email, !userEmail.isEmpty {
                email = userEmail
            }
        }
        .onReceive(Timer.publish(every: 1, on: .main, in: .common).autoconnect()) { _ in
            if countdown > 0 { countdown -= 1 }
        }
        .onChange(of: viewModel.authError) { _, newValue in
            if let msg = newValue {
                toastMessage = msg
                viewModel.clearAuthError()
                isLoading = false
            }
        }
        .onChange(of: viewModel.changePasswordSuccess) { _, success in
            if success {
                viewModel.clearChangePasswordSuccess()
                showSuccessAlert = true
            }
        }
        .toast(message: $toastMessage, type: .error)
        .alert("密码修改成功", isPresented: $showSuccessAlert) {
            Button("确定", role: .cancel) {
                dismiss()
            }
        }
    }

    private var verifyStepContent: some View {
        Form {
            Section {
                Text("我们将向您的邮箱发送验证码，请先完成验证")
                    .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }

            Section("邮箱") {
                TextField("邮箱", text: $email)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                    .disabled(viewModel.currentUser?.email != nil && !(viewModel.currentUser?.email?.isEmpty ?? true))
            }

            Section("验证码") {
                HStack {
                    TextField("请输入验证码", text: $emailCode)
                        .keyboardType(.numberPad)
                    Button(countdown > 0 ? "\(countdown)s" : "获取验证码") {
                        if email.isEmpty {
                            toastMessage = "请先输入邮箱"
                            return
                        }
                        viewModel.sendEmailCode(email: email, type: "reset")
                        countdown = 60
                        toastMessage = "验证码已发送，请查收邮箱"
                    }
                    .disabled(countdown > 0)
                    .foregroundColor(AppTheme.Colors.primary)
                }
            }

            Section {
                Button("下一步") {
                    if emailCode.isEmpty {
                        toastMessage = "请输入验证码"
                        return
                    }
                    step = 2
                }
                .frame(maxWidth: .infinity)
                .foregroundColor(.white)
            }
        }
    }

    private var passwordStepContent: some View {
        Form {
            Section {
                Text("请输入当前密码并设置新密码")
                    .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }

            Section("当前密码") {
                SecureField("请输入当前密码", text: $oldPassword)
            }

            Section("新密码") {
                SecureField("请输入新密码", text: $newPassword)
                SecureField("请再次输入新密码", text: $confirmPassword)
            }

            Section {
                Button(action: submitChangePassword) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("完成")
                            .frame(maxWidth: .infinity)
                    }
                }
                .disabled(isLoading)
                .foregroundColor(.white)

                Button("返回上一步") {
                    step = 1
                }
                .frame(maxWidth: .infinity)
                .foregroundColor(AppTheme.Colors.primary)
            }
        }
    }

    private func submitChangePassword() {
        if oldPassword.isEmpty {
            toastMessage = "请输入当前密码"
            return
        }
        if newPassword.isEmpty {
            toastMessage = "请输入新密码"
            return
        }
        if newPassword != confirmPassword {
            toastMessage = "两次输入的新密码不一致"
            return
        }
        if newPassword.count < 6 {
            toastMessage = "新密码至少 6 位"
            return
        }
        isLoading = true
        viewModel.changePassword(
            email: email,
            emailCode: emailCode,
            oldPassword: oldPassword,
            newPassword: newPassword
        )
    }
}

#if DEBUG
struct ChangePasswordView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ChangePasswordView(viewModel: AppViewModel())
        }
    }
}
#endif
