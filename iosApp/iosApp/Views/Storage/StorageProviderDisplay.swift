import Foundation
import Shared

/// 存储提供商显示名称（与 Android getProviderDisplayName 一致）
func storageProviderDisplayName(_ provider: StorageProvider) -> String {
    switch provider {
    case .aliyunOss: return "阿里云 OSS"
    case .awsS3: return "AWS S3"
    case .tencentCos: return "腾讯云 COS"
    case .minio: return "MinIO"
    case .qiniu: return "七牛云"
    case .customS3: return "自定义 S3"
    default: return String(describing: provider)
    }
}
