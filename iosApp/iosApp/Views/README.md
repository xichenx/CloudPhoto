# iOS 视图目录结构

按功能拆分，便于维护与扩展。

## 目录说明

```
Views/
├── Photos/           # 照片 Tab
│   └── PhotosView.swift
├── Albums/           # 相册 Tab
│   └── AlbumsView.swift
├── Camera/           # 拍照 Tab
│   ├── CameraView.swift
│   └── ImagePickerBridge.swift   # UIKit 相机/相册封装
├── Storage/          # 空间 Tab 及二级页
│   ├── StorageRoute.swift        # 存储内路由枚举
│   ├── StorageView.swift
│   ├── StorageConfigRow.swift    # 配置行 + ConfigInfoRow
│   └── AddConfigView.swift
├── Settings/         # 我的 Tab
│   └── SettingsView.swift
├── Components/       # 通用 UI 组件
│   └── FeatureRow.swift
└── README.md         # 本说明
```

## 约定

- **每个 Tab 一个目录**，主屏与二级屏放同一目录（如 Storage 含 StorageView、AddConfigView）。
- **路由/类型**：仅本模块用的枚举放该目录（如 `StorageRoute`）。
- **跨屏组件**：放 `Components/`，或按需新建子目录。
- **UIKit 桥接**：与某 Tab 强相关的放该 Tab 目录（如 `ImagePickerBridge` 在 Camera）。

## 根入口

- `ContentView.swift`：仅保留根容器、AuthFlowView、MainTabView，不承载具体业务视图。
