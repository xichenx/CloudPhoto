import SwiftUI
import Shared

/**
 * 根界面 - 未登录显示登录/注册，已登录显示主 Tab
 */
struct ContentView: View {
    @StateObject private var viewModel = AppViewModel()

    var body: some View {
        ThemedView {
            if viewModel.isLoggedIn {
                MainTabView(viewModel: viewModel)
            } else {
                AuthFlowView(viewModel: viewModel)
            }
        }
    }
}

/**
 * 登录/注册流程 - 在登录页与注册页之间切换
 */
struct AuthFlowView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var showRegister = false

    var body: some View {
        if showRegister {
            RegisterView(viewModel: viewModel, onNavigateToLogin: {
                showRegister = false
            })
            .overlay(alignment: .topLeading) {
                Button(action: { showRegister = false }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(AppTheme.Colors.primary)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                }
                .padding(.top, 8)
            }
        } else {
            LoginView(viewModel: viewModel, onNavigateToRegister: {
                showRegister = true
            })
        }
    }
}

/**
 * 主界面 - TabView 底部导航（已登录后显示）
 */
struct MainTabView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var selectedTab = 0
    @State private var previousTabBeforeCamera = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            PhotosView(viewModel: viewModel)
                .tabItem {
                    Label("照片", systemImage: "photo")
                }
                .tag(0)

            AlbumsView()
                .tabItem {
                    Label("相册", systemImage: "photo.on.rectangle")
                }
                .tag(1)

            CameraView(viewModel: viewModel, onCameraDismissed: {
                selectedTab = previousTabBeforeCamera
            })
                .tabItem {
                    Label("拍照", systemImage: "camera.fill")
                }
                .tag(2)

            StorageView(viewModel: viewModel)
                .tabItem {
                    Label("空间", systemImage: "folder")
                }
                .tag(3)

            SettingsView(viewModel: viewModel)
                .tabItem {
                    Label("我的", systemImage: "person.circle")
                }
                .tag(4)
        }
        .tint(AppTheme.Colors.primary)
        .onChange(of: selectedTab) { _, newValue in
            if newValue != 2 {
                previousTabBeforeCamera = newValue
            }
        }
    }
}

// MARK: - Previews

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
