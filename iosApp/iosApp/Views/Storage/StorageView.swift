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
                if route == .addConfig {
                    AddConfigView(viewModel: viewModel, path: $path)
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
            Button(action: { path.append(.addConfig) }) {
                Text("添加配置")
                    .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 200, height: 44)
                    .background(AppTheme.Colors.primary)
                    .cornerRadius(AppTheme.Design.cornerRadiusMedium)
            }
            .padding(.top, AppTheme.Design.spacingM)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.Colors.background)
        .navigationTitle("存储空间")
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { path.append(.addConfig) }) {
                    Image(systemName: "plus")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }

    private var storageListContent: some View {
        List {
            Section {
                HStack {
                    VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                        Text("存储配置")
                            .font(.system(size: AppTheme.Design.fontSizeTitle3, weight: .semibold))
                        Text("共 \(viewModel.configs.count) 个配置")
                            .font(.system(size: AppTheme.Design.fontSizeBody))
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                    Spacer()
                    Image(systemName: "folder.fill")
                        .font(.system(size: 40))
                        .foregroundColor(AppTheme.Colors.primary)
                }
                .padding(.vertical, AppTheme.Design.spacingXS)
            }

            Section("存储配置") {
                ForEach(viewModel.configs, id: \.id) { config in
                    StorageConfigRow(
                        config: config,
                        isDefault: config.id == viewModel.defaultConfig?.id,
                        onSetDefault: { viewModel.setDefaultConfig(configId: config.id) },
                        onDelete: { viewModel.deleteConfig(configId: config.id) }
                    )
                }
            }
        }
        .navigationTitle("存储空间")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { path.append(.addConfig) }) {
                    Image(systemName: "plus")
                        .foregroundColor(AppTheme.Colors.primary)
                }
            }
        }
    }
}
