import SwiftUI
import Shared

/**
 * 存储视图 - 遵循 Apple HIG 设计规范
 * 使用 NavigationStack + path：仅在根界面显示 Tab 栏，二级界面（如添加配置）隐藏 Tab 栏。
 */
struct StorageView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var path: [StorageRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if viewModel.configs.isEmpty {
                    storageEmptyContent
                } else {
                    storageListContent
                }
            }
            .navigationDestination(for: StorageRoute.self) { route in
                switch route {
                case .addConfig:
                    AddConfigView(viewModel: viewModel, configToEdit: nil, path: $path)
                case .editConfig(let configId):
                    let config = viewModel.configs.first { $0.id == configId }
                    AddConfigView(viewModel: viewModel, configToEdit: config, path: $path)
                case .configTutorial:
                    StorageTutorialWebView(viewModel: viewModel, path: $path)
                }
            }
        }
        .toolbar(path.isEmpty ? .visible : .hidden, for: .tabBar)
    }

    private var storageEmptyContent: some View {
        VStack(spacing: AppTheme.Design.spacingM) {
            Image(systemName: "folder")
                .font(.system(size: 64))
                .foregroundColor(AppTheme.Colors.secondaryText)
            Text("暂无存储配置")
                .font(.system(size: AppTheme.Design.fontSizeTitle3, weight: .semibold))
                .foregroundColor(AppTheme.Colors.text)
            Text("添加存储配置以开始备份照片")
                .font(.system(size: AppTheme.Design.fontSizeBody))
                .foregroundColor(AppTheme.Colors.secondaryText)
            Button(action: {
                viewModel.trackStorageAddConfig(elementName: "添加首个配置")
                path.append(.addConfig)
            }) {
                Text("添加配置")
                    .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 200, height: 44)
                    .background(AppTheme.Colors.primary)
                    .cornerRadius(AppTheme.Design.cornerRadiusMedium)
            }
            .padding(.top, AppTheme.Design.spacingM)
            Button(action: {
                viewModel.trackStorageTutorial()
                path.append(.configTutorial)
            }) {
                Label("各厂商获取配置教程", systemImage: "book.circle")
                    .font(.system(size: AppTheme.Design.fontSizeBody))
                    .foregroundColor(AppTheme.Colors.primary)
            }
            .padding(.top, AppTheme.Design.spacingS)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.Colors.background)
        .navigationTitle("存储空间")
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.trackStorageAddConfig(elementName: "添加存储配置")
                    path.append(.addConfig)
                }) {
                    Image(systemName: "plus")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }

    private var storageListContent: some View {
        List {
            Section {
                Button(action: {
                    viewModel.trackStorageTutorial()
                    path.append(.configTutorial)
                }) {
                    HStack(spacing: AppTheme.Design.spacingS) {
                        Image(systemName: "book.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(AppTheme.Colors.primary)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("各厂商获取配置教程")
                                .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .medium))
                                .foregroundColor(AppTheme.Colors.text)
                            Text("阿里云 OSS、腾讯云 COS、AWS S3 等配置说明")
                                .font(.system(size: AppTheme.Design.fontSizeCaption))
                                .foregroundColor(AppTheme.Colors.secondaryText)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                    .padding(.vertical, AppTheme.Design.spacingXS)
                }
                .buttonStyle(.plain)
            }

            Section("存储配置") {
                ForEach(viewModel.configs, id: \.id) { config in
                    StorageConfigRow(
                        config: config,
                        isDefault: config.id == viewModel.defaultConfig?.id,
                        onEdit: {
                            viewModel.trackStorageEditConfig(configId: config.id)
                            path.append(.editConfig(configId: config.id))
                        },
                        onSetDefault: { viewModel.setDefaultConfig(configId: config.id) },
                        onDelete: { viewModel.deleteConfig(configId: config.id) }
                    )
                }
            }
        }
        .navigationTitle("存储空间")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.trackStorageAddConfig(elementName: "添加存储配置")
                    path.append(.addConfig)
                }) {
                    Image(systemName: "plus")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }
}
