import SwiftUI
import UIKit
import Shared

/**
 * 帮助与反馈：常见问题（与 KMP [HelpFeedbackCopy] 同源）、提交反馈 API、邮件与教程外链。
 */
struct HelpFeedbackView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var contentText = ""
    @State private var category = "general"
    @State private var toastMessage: String?
    @State private var toastType: ToastType = .info

    private let categoryOptions: [(label: String, value: String)] = [
        ("问题反馈", "bug"),
        ("功能建议", "suggestion"),
        ("其他", "general"),
    ]

    /// KMP 将 `faqItems` 桥成 Swift `[HelpFaqItem]`，直接用数组 API 即可。
    private var faqPairs: [(String, String)] {
        HelpFeedbackCopy.shared.faqItems.map { ($0.question, $0.answer) }
    }

    var body: some View {
        List {
            Section {
                ForEach(Array(faqPairs.enumerated()), id: \.offset) { _, pair in
                    VStack(alignment: .leading, spacing: AppTheme.Design.spacingS) {
                        Text(pair.0)
                            .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .semibold))
                            .foregroundColor(AppTheme.Colors.text)
                        Text(pair.1)
                            .font(.system(size: AppTheme.Design.fontSizeBody))
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                    .padding(.vertical, AppTheme.Design.spacingXS)
                }
            } header: {
                Text("常见问题")
            }

            Section {
                Button(action: openTutorialInBrowser) {
                    Label("查看存储配置教程（网页）", systemImage: "safari")
                }
            }

            Section {
                Picker("分类", selection: $category) {
                    ForEach(categoryOptions, id: \.value) { opt in
                        Text(opt.label).tag(opt.value)
                    }
                }
                .pickerStyle(.segmented)

                ZStack(alignment: .topLeading) {
                    if contentText.isEmpty {
                        Text("请描述问题或建议…")
                            .foregroundColor(AppTheme.Colors.secondaryText.opacity(0.6))
                            .padding(.top, 8)
                            .padding(.leading, 4)
                    }
                    TextEditor(text: $contentText)
                        .frame(minHeight: 120)
                }

                Button(action: submit) {
                    HStack {
                        if viewModel.feedbackSubmitting {
                            ProgressView()
                                .scaleEffect(0.9)
                                .padding(.trailing, 6)
                        }
                        Text("提交反馈")
                            .frame(maxWidth: .infinity)
                    }
                }
                .disabled(viewModel.feedbackSubmitting)
            } header: {
                Text("提交反馈")
            } footer: {
                Text("反馈内容 5～2000 字；提交需已登录，账号信息由服务端关联。")
                    .font(.footnote)
                    .foregroundColor(AppTheme.Colors.secondaryText)
            }
        }
        .navigationTitle("帮助与反馈")
        .navigationBarTitleDisplayMode(.inline)
        .toast(message: $toastMessage, type: toastType)
        .onAppear {
            viewModel.trackPageViewHelpFeedback()
        }
        .onChange(of: viewModel.feedbackSuccess) { _, ok in
            if ok {
                toastType = .success
                toastMessage = "感谢反馈，我们已收到"
                contentText = ""
                viewModel.clearFeedbackUiState()
            }
        }
        .onChange(of: viewModel.feedbackError) { _, msg in
            guard let msg else { return }
            toastType = .error
            toastMessage = msg
            viewModel.clearFeedbackUiState()
        }
    }

    private func submit() {
        viewModel.trackClick(
            page: AppAnalyticsCatalog.Page.helpFeedback,
            eventId: AppAnalyticsCatalog.EventId.feedbackSubmit,
            elementType: "button",
            elementName: "提交反馈"
        )
        viewModel.submitFeedback(content: contentText, category: category)
    }

    private func openTutorialInBrowser() {
        let s = StorageTutorialUrls.shared.value()
        guard let url = URL(string: s) else { return }
        UIApplication.shared.open(url)
    }
}
