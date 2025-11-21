import SwiftUI
import Shared

/**
 * 添加存储配置 - 独立全屏界面（由存储页 push 进入，与 Android 一致）
 * 通过 path Binding 出栈，便于根界面根据 path.isEmpty 控制 Tab 栏显隐。
 */
struct AddConfigView: View {
    @ObservedObject var viewModel: AppViewModel
    @Binding var path: [StorageRoute]
    @State private var name = ""
    @State private var endpoint = ""
    @State private var accessKeyId = ""
    @State private var accessKeySecret = ""
    @State private var bucketName = ""
    @State private var region = ""
    @State private var isDefault = false
    @State private var selectedProvider: StorageProvider = .aliyunOss

    var body: some View {
        Form {
            Section("基本信息") {
                TextField("配置名称", text: $name)
                Picker("提供商", selection: $selectedProvider) {
                    Text("阿里云OSS").tag(StorageProvider.aliyunOss)
                    Text("AWS S3").tag(StorageProvider.awsS3)
                    Text("腾讯云COS").tag(StorageProvider.tencentCos)
                    Text("MinIO").tag(StorageProvider.minio)
                    Text("自定义S3").tag(StorageProvider.customS3)
                }
            }
            Section("连接信息") {
                TextField("Endpoint", text: $endpoint)
                TextField("Access Key ID", text: $accessKeyId)
                SecureField("Access Key Secret", text: $accessKeySecret)
                TextField("Bucket Name", text: $bucketName)
                TextField("Region (可选)", text: $region)
            }
            Section {
                Toggle("设为默认", isOn: $isDefault)
            }
        }
        .navigationTitle("添加存储配置")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("保存") {
                    let config = StorageConfig(
                        id: "\(Int(Date().timeIntervalSince1970))_\(Int.random(in: 0...999999))",
                        name: name,
                        provider: selectedProvider,
                        endpoint: endpoint,
                        accessKeyId: accessKeyId,
                        accessKeySecret: accessKeySecret,
                        bucketName: bucketName,
                        region: region.isEmpty ? nil : region,
                        isDefault: isDefault,
                        createdAt: Int64(Date().timeIntervalSince1970)
                    )
                    viewModel.saveConfig(config: config)
                    path.removeAll()
                }
                .disabled(name.isEmpty || endpoint.isEmpty || accessKeyId.isEmpty || accessKeySecret.isEmpty || bucketName.isEmpty)
            }
        }
    }
}
