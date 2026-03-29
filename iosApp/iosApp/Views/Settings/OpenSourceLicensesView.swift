import SwiftUI

/// 开源组件许可说明（与 Android [OpenSourceLicensesScreen] 文案一致）。
private let openSourceNotice = """
CloudPhoto 使用了以下开源软件（节选）。感谢开源社区。

— Kotlin / Kotlin Multiplatform
  Apache License 2.0
  https://kotlinlang.org/

— kotlinx-coroutines, kotlinx-serialization, kotlinx-datetime
  Apache License 2.0
  https://github.com/Kotlin/

— Ktor Client
  Apache License 2.0
  https://github.com/ktorio/ktor

— Napier
  Apache License 2.0
  https://github.com/AAkira/Napier

— Android Jetpack (Compose, Material3, Navigation, Lifecycle 等)
  Apache License 2.0
  https://developer.android.com/

— Coil
  Apache License 2.0
  https://github.com/coil-kt/coil

— Accompanist (System UI Controller 等)
  Apache License 2.0
  https://github.com/google/accompanist

— OkHttp / Okio（经 Ktor 引擎）
  Apache License 2.0
  https://square.github.io/okhttp/

— Firebase（国际版依赖，见 Google 条款）
  https://firebase.google.com/terms

— iOS: SwiftUI、Nuke / NukeUI 等
  以 Xcode / Swift Package 及各自仓库许可证为准。

本列表随依赖更新可能变化；若需某一库的完整许可证文本，请查阅其官方仓库或随包分发材料。
"""

struct OpenSourceLicensesView: View {
    var body: some View {
        ScrollView {
            Text(openSourceNotice)
                .font(.system(size: AppTheme.Design.fontSizeSubheadline, design: .monospaced))
                .foregroundColor(AppTheme.Colors.text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(AppTheme.Design.spacingM)
        }
        .background(AppTheme.Colors.background)
        .navigationTitle("开源组件许可")
        .navigationBarTitleDisplayMode(.inline)
    }
}
