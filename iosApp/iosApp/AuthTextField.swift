import SwiftUI

/// 认证页通用输入框 - 带图标、错误提示、可选明文/密码
struct AuthTextField<Trailing: View>: View {
    let label: String
    @Binding var text: String
    @Binding var error: String?
    var icon: String = "person"
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var textContentType: UITextContentType?
    var submitLabel: SubmitLabel = .next
    @ViewBuilder var trailing: () -> Trailing
    var onSubmit: () -> Void = {}
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(error != nil ? AppTheme.Colors.error : AppTheme.Colors.primary)
                    .frame(width: 22, alignment: .center)
                
                if isSecure {
                    SecureField(label, text: $text)
                        .textContentType(textContentType)
                        .keyboardType(keyboardType)
                        .submitLabel(submitLabel)
                        .onSubmit(onSubmit)
                        .onChange(of: text) { _, _ in error = nil }
                } else {
                    TextField(label, text: $text)
                        .textContentType(textContentType)
                        .keyboardType(keyboardType)
                        .submitLabel(submitLabel)
                        .onSubmit(onSubmit)
                        .onChange(of: text) { _, _ in error = nil }
                }
                
                trailing()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(AppTheme.Colors.surface)
            .overlay(
                RoundedRectangle(cornerRadius: AppTheme.Design.cornerRadiusLarge)
                    .stroke(error != nil ? AppTheme.Colors.error : AppTheme.Colors.secondaryText.opacity(0.35), lineWidth: 1)
            )
            .cornerRadius(AppTheme.Design.cornerRadiusLarge)
            
            if let msg = error {
                HStack(spacing: 6) {
                    Image(systemName: "exclamationmark.circle")
                        .font(.system(size: 14))
                        .foregroundColor(AppTheme.Colors.error)
                    Text(msg)
                        .font(.system(size: AppTheme.Design.fontSizeFootnote))
                        .foregroundColor(AppTheme.Colors.error)
                }
                .padding(.leading, 16)
            }
        }
    }
}

extension AuthTextField where Trailing == EmptyView {
    init(
        label: String,
        text: Binding<String>,
        error: Binding<String?>,
        icon: String = "person",
        isSecure: Bool = false,
        keyboardType: UIKeyboardType = .default,
        textContentType: UITextContentType? = nil,
        submitLabel: SubmitLabel = .next,
        onSubmit: @escaping () -> Void = {}
    ) {
        self.label = label
        self._text = text
        self._error = error
        self.icon = icon
        self.isSecure = isSecure
        self.keyboardType = keyboardType
        self.textContentType = textContentType
        self.submitLabel = submitLabel
        self.trailing = { EmptyView() }
        self.onSubmit = onSubmit
    }
}
