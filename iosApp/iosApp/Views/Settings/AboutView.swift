import SwiftUI
import Shared

/**
 * 关于：开源组件许可、上传日志、当前版本。
 */
struct AboutView: View {
    @ObservedObject var viewModel: AppViewModel
    @Binding var path: [SettingsRoute]
    @State private var toastMessage: String?
    @State private var toastType: ToastType = .info
    @State private var showUploadLogConfirm = false

    private var appVersion: String {
        (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "—"
    }

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: AppTheme.Design.spacingS) {
                    Text("CloudPhoto")
                        .font(.system(size: AppTheme.Design.fontSizeTitle2, weight: .bold))
                        .foregroundColor(AppTheme.Colors.text)
                    Text("云相册")
                        .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                        .foregroundColor(AppTheme.Colors.secondaryText)
                }
                .padding(.vertical, AppTheme.Design.spacingXS)
            }

            Section {
                Button {
                    viewModel.trackClick(
                        page: AppAnalyticsCatalog.Page.about,
                        eventId: AppAnalyticsCatalog.EventId.aboutOpenSourceLicenses,
                        elementType: "list_item",
                        elementName: "开源组件许可"
                    )
                    path.append(.openSourceLicenses)
                } label: {
                    HStack {
                        Label("开源组件许可", systemImage: "doc.text")
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)

                Button {
                    showUploadLogConfirm = true
                } label: {
                    HStack {
                        Label(
                            viewModel.diagnosticLogUploading ? "正在上传…" : "上传日志",
                            systemImage: "arrow.up.doc"
                        )
                        Spacer()
                        if viewModel.diagnosticLogUploading {
                            ProgressView()
                        } else {
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(viewModel.diagnosticLogUploading)

                HStack {
                    Label("当前版本", systemImage: "info.circle")
                    Spacer()
                    Text(appVersion)
                        .font(.system(size: AppTheme.Design.fontSizeBody))
                        .foregroundColor(AppTheme.Colors.secondaryText)
                }
                .listRowInsets(EdgeInsets(
                    top: AppTheme.Design.spacingS,
                    leading: AppTheme.Design.spacingM,
                    bottom: AppTheme.Design.spacingS,
                    trailing: AppTheme.Design.spacingM
                ))
            } footer: {
                Text("上传日志需登录；诊断数据用于问题排查。")
                    .font(.footnote)
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }
        }
        .navigationTitle("关于")
        .navigationBarTitleDisplayMode(.inline)
        .toast(message: $toastMessage, type: toastType)
        .onAppear {
            viewModel.trackPageViewAbout()
        }
        .onChange(of: viewModel.diagnosticLogToast) { _, msg in
            guard let msg else { return }
            toastType = viewModel.diagnosticLogToastType
            toastMessage = msg
            viewModel.clearDiagnosticLogToast()
        }
        .alert("上传诊断日志？", isPresented: $showUploadLogConfirm) {
            Button("上传") {
                viewModel.trackClick(
                    page: AppAnalyticsCatalog.Page.about,
                    eventId: AppAnalyticsCatalog.EventId.aboutUploadLogs,
                    elementType: "button",
                    elementName: "确认上传日志"
                )
                viewModel.uploadDiagnosticLogsNow()
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("将本机缓存的诊断日志上传到服务器，用于问题排查。需登录账号。")
        }
    }
}
