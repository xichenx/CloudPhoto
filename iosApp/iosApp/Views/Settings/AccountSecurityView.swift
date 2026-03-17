import SwiftUI
import Shared

/**
 * 账号安全页 - 修改密码、登录设备、安全提示，遵循 Apple HIG 设计规范
 */
struct AccountSecurityView: View {
    @ObservedObject var viewModel: AppViewModel

    var body: some View {
        List {
            Section("密码与登录") {
                NavigationLink(value: SettingsRoute.changePassword) {
                    SecurityRow(
                        title: "修改密码",
                        subtitle: "定期更换密码可提升账号安全性",
                        icon: "lock.fill"
                    )
                }

                SecurityRow(
                    title: "登录设备管理",
                    subtitle: "暂无其他登录设备",
                    icon: "desktopcomputer"
                )
            }

            Section("安全提示") {
                SecurityTipRow(text: "请勿与他人共享账号，避免数据泄露")
                SecurityTipRow(text: "建议使用字母、数字与符号组合的强密码")
                SecurityTipRow(text: "发现异常登录请及时修改密码并联系客服")
            }
        }
        .navigationTitle("账号安全")
        .navigationBarTitleDisplayMode(.inline)
    }
}

/**
 * 安全设置行 - 标题 + 副标题 + 图标
 */
private struct SecurityRow: View {
    let title: String
    let subtitle: String
    let icon: String

    var body: some View {
        HStack(spacing: AppTheme.Design.spacingM) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(AppTheme.Colors.primary)
                .frame(width: 24, alignment: .center)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: AppTheme.Design.fontSizeBody, weight: .medium))
                    .foregroundColor(AppTheme.Colors.text)
                Text(subtitle)
                    .font(.system(size: AppTheme.Design.fontSizeFootnote))
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 4)
    }
}

/**
 * 安全提示行
 */
private struct SecurityTipRow: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: AppTheme.Design.spacingS) {
            Image(systemName: "info.circle.fill")
                .font(.system(size: 16))
                .foregroundColor(AppTheme.Colors.primary.opacity(0.8))

            Text(text)
                .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                .foregroundColor(AppTheme.Colors.text)
        }
        .padding(.vertical, 4)
    }
}

#if DEBUG
struct AccountSecurityView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            AccountSecurityView(viewModel: AppViewModel())
        }
    }
}
#endif
