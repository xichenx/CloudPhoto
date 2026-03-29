import SwiftUI
import Shared

/**
 * 消息通知：云端是否向本账号下发推送（与系统通知权限无关）。
 */
struct NotificationSettingsView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var toastMessage: String?
    @State private var toastType: ToastType = .info

    var body: some View {
        List {
            Section {
                Text("由云端决定是否向您推送消息（如备份完成、活动提醒等）。关闭后服务端将不再向您下发推送；与系统「通知权限」无关。")
                    .font(.system(size: AppTheme.Design.fontSizeFootnote))
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }

            Section {
                HStack(alignment: .center, spacing: AppTheme.Design.spacingM) {
                    Image(systemName: "bell.badge")
                        .font(.system(size: 22))
                        .foregroundColor(AppTheme.Colors.primary)
                        .frame(width: 28, alignment: .center)

                    VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                        Text("接收云端推送")
                            .font(.system(size: AppTheme.Design.fontSizeBody, weight: .medium))
                            .foregroundColor(AppTheme.Colors.text)
                        Text("开启后服务端可在您允许时向您发送推送")
                            .font(.system(size: AppTheme.Design.fontSizeFootnote))
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    if viewModel.cloudPushPreferenceLoading {
                        ProgressView()
                    } else {
                        Toggle(
                            "接收云端推送",
                            isOn: Binding(
                                get: { viewModel.cloudPushEnabled == true },
                                set: { newValue in
                                    viewModel.setCloudPushEnabled(newValue)
                                }
                            )
                        )
                        .labelsHidden()
                        .toggleStyle(.switch)
                        .accessibilityLabel("接收云端推送")
                        .disabled(
                            !viewModel.isLoggedIn
                                || viewModel.cloudPushEnabled == nil
                                || viewModel.cloudPushPreferenceSaving
                        )
                    }
                }
                .padding(.vertical, AppTheme.Design.spacingXS)
            } footer: {
                if viewModel.cloudPushPreferenceSaving {
                    Text("正在保存…")
                        .font(.footnote)
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
        .navigationTitle("消息通知")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.trackPageViewNotificationSettings()
            viewModel.loadCloudPushPreference()
        }
        .toast(message: $toastMessage, type: toastType)
        .onChange(of: viewModel.pushPreferenceToast) { _, msg in
            guard let msg else { return }
            toastType = viewModel.pushPreferenceToastType
            toastMessage = msg
            viewModel.clearPushPreferenceToast()
        }
    }
}
