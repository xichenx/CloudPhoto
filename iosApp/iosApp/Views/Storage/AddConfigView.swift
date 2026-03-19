import SwiftUI
import Shared

/**
 * 添加/编辑存储配置 - 独立全屏界面（与 Android AddStorageConfigScreen 一致）
 * 通过 path Binding 出栈，便于根界面根据 path.isEmpty 控制 Tab 栏显隐。
 */
struct AddConfigView: View {
    @ObservedObject var viewModel: AppViewModel
    let configToEdit: StorageConfig?
    @Binding var path: [StorageRoute]

    @State private var name = ""
    @State private var endpoint = ""
    @State private var accessKeyId = ""
    @State private var accessKeySecret = ""
    @State private var bucketName = ""
    @State private var region = ""
    @State private var isDefault = false
    @State private var selectedProvider: StorageProvider = .aliyunOss
    @State private var accessKeySecretVisible = false

    private var isEditMode: Bool { configToEdit != nil }

    init(viewModel: AppViewModel, configToEdit: StorageConfig?, path: Binding<[StorageRoute]>) {
        self.viewModel = viewModel
        self.configToEdit = configToEdit
        self._path = path
        if let c = configToEdit {
            _name = State(initialValue: c.name)
            _endpoint = State(initialValue: c.endpoint)
            _accessKeyId = State(initialValue: c.accessKeyId)
            _accessKeySecret = State(initialValue: c.accessKeySecret)
            _bucketName = State(initialValue: c.bucketName)
            _region = State(initialValue: c.region ?? "")
            _isDefault = State(initialValue: c.isDefault)
            _selectedProvider = State(initialValue: c.provider)
        }
    }

    private var canSave: Bool {
        !name.isEmpty && !endpoint.isEmpty &&
        !accessKeyId.isEmpty && !accessKeySecret.isEmpty &&
        !bucketName.isEmpty
    }

    private func save() {
        guard canSave else { return }
        let config = StorageConfig(
            id: configToEdit?.id ?? "\(Int(Date().timeIntervalSince1970))_\(Int.random(in: 0...999999))",
            name: name,
            provider: selectedProvider,
            endpoint: endpoint,
            accessKeyId: accessKeyId,
            accessKeySecret: accessKeySecret,
            bucketName: bucketName,
            region: region.isEmpty ? nil : region,
            isDefault: isDefault,
            createdAt: configToEdit?.createdAt ?? Int64(Date().timeIntervalSince1970)
        )
        viewModel.saveConfig(config: config)
        path.removeAll()
    }

    var body: some View {
        Form {
            Section("基本信息") {
                if isEditMode {
                    HStack {
                        Text("存储提供商")
                            .foregroundColor(AppTheme.Colors.secondaryText)
                        Spacer()
                        Text(storageProviderDisplayName(selectedProvider))
                            .foregroundColor(AppTheme.Colors.text)
                    }
                    .font(.system(size: AppTheme.Design.fontSizeBody))
                } else {
                    Picker("存储提供商", selection: $selectedProvider) {
                        Text(storageProviderDisplayName(.aliyunOss)).tag(StorageProvider.aliyunOss)
                        Text(storageProviderDisplayName(.awsS3)).tag(StorageProvider.awsS3)
                        Text(storageProviderDisplayName(.tencentCos)).tag(StorageProvider.tencentCos)
                        Text(storageProviderDisplayName(.minio)).tag(StorageProvider.minio)
                        Text(storageProviderDisplayName(.qiniu)).tag(StorageProvider.qiniu)
                        Text(storageProviderDisplayName(.customS3)).tag(StorageProvider.customS3)
                    }
                }
                TextField("配置名称", text: $name)
            }
            Section("连接信息") {
                TextField("Endpoint", text: $endpoint)
                TextField("Access Key ID", text: $accessKeyId)
                HStack {
                    if accessKeySecretVisible {
                        TextField("Access Key Secret", text: $accessKeySecret)
                    } else {
                        SecureField("Access Key Secret", text: $accessKeySecret)
                    }
                    Button(action: { accessKeySecretVisible.toggle() }) {
                        Image(systemName: accessKeySecretVisible ? "eye.slash.fill" : "eye.fill")
                            .foregroundColor(AppTheme.Colors.secondaryText)
                    }
                }
                TextField("Bucket Name", text: $bucketName)
                TextField("Region (可选)", text: $region)
            }
            Section {
                Toggle("设为默认", isOn: $isDefault)
            }
        }
        .navigationTitle(isEditMode ? "编辑存储配置" : "添加存储配置")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("保存", action: save)
                    .disabled(!canSave)
            }
        }
    }
}
