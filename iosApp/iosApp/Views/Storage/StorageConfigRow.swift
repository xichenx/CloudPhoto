import SwiftUI
import Shared

/**
 * 存储配置行 - 遵循 Apple HIG 设计规范
 */
struct StorageConfigRow: View {
    let config: StorageConfig
    let isDefault: Bool
    var onEdit: (() -> Void)? = nil
    let onSetDefault: () -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Design.spacingS) {
            HStack {
                ZStack {
                    Circle()
                        .fill(AppTheme.Colors.primary.opacity(0.1))
                        .frame(width: 48, height: 48)
                    Image(systemName: providerSystemImage)
                        .foregroundColor(AppTheme.Colors.primary)
                        .font(.system(size: 20))
                }

                VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                    HStack {
                        Text(config.name)
                            .font(.system(size: AppTheme.Design.fontSizeHeadline, weight: .semibold))
                            .foregroundColor(AppTheme.Colors.text)
                        if isDefault {
                            Text("默认")
                                .font(.system(size: AppTheme.Design.fontSizeCaption, weight: .semibold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(AppTheme.Colors.primary.opacity(0.1))
                                .foregroundColor(AppTheme.Colors.primary)
                                .cornerRadius(AppTheme.Design.cornerRadiusSmall)
                        }
                    }
                    Text(storageProviderDisplayName(config.provider))
                        .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                        .foregroundColor(AppTheme.Colors.secondaryText)
                }

                Spacer()
            }

            VStack(alignment: .leading, spacing: AppTheme.Design.spacingXS) {
                ConfigInfoRow(label: "Bucket", value: config.bucketName)
                if let region = config.region {
                    ConfigInfoRow(label: "Region", value: region)
                }
            }

            Divider()

            HStack(spacing: AppTheme.Design.spacingS) {
                if let onEdit = onEdit {
                    Button("编辑", action: onEdit)
                        .buttonStyle(.bordered)
                        .tint(AppTheme.Colors.primary)
                }
                if !isDefault {
                    Button("设为默认", action: onSetDefault)
                        .buttonStyle(.bordered)
                        .tint(AppTheme.Colors.primary)
                }
                Button("删除", action: onDelete)
                    .buttonStyle(.bordered)
                    .tint(AppTheme.Colors.error)
            }
        }
        .padding(.vertical, AppTheme.Design.spacingS)
    }

    private var providerSystemImage: String {
        switch config.provider {
        case .minio: return "folder.fill"
        default: return "cloud.fill"
        }
    }
}

/**
 * 配置信息行
 */
struct ConfigInfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: AppTheme.Design.fontSizeSubheadline))
                .foregroundColor(AppTheme.Colors.secondaryText)
            Spacer()
            Text(value)
                .font(.system(size: AppTheme.Design.fontSizeSubheadline, weight: .medium))
                .foregroundColor(AppTheme.Colors.text)
        }
    }
}
