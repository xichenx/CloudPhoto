import SwiftUI
import UIKit
import WebKit
import Shared

struct StorageTutorialWebView: View {
    @ObservedObject var viewModel: AppViewModel
    @Binding var path: [StorageRoute]
    @State private var isLoading = true

    var body: some View {
        ZStack(alignment: .top) {
            WebView(urlString: StorageTutorialUrls.shared.value(), isLoading: $isLoading)
            if isLoading {
                ProgressView()
                    .scaleEffect(1.2)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(AppTheme.Colors.background)
            }
        }
        .navigationTitle("配置教程")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                if !isLoading {
                    Button(action: { path.removeLast() }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                }
            }
        }
        .onAppear {
            viewModel.trackPageViewStorageTutorial()
        }
    }
}

private struct WebView: UIViewRepresentable {
    let urlString: String
    @Binding var isLoading: Bool

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        guard let url = URL(string: urlString), webView.url != url else { return }
        webView.load(URLRequest(url: url))
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(isLoading: $isLoading)
    }

    class Coordinator: NSObject, WKNavigationDelegate {
        @Binding var isLoading: Bool

        init(isLoading: Binding<Bool>) {
            _isLoading = isLoading
        }

        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            isLoading = true
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            isLoading = false
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            isLoading = false
        }
    }
}
