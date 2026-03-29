package com.xichen.cloudphoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * 开源组件许可说明（摘要；完整许可证文本以各组件发布源为准）。
 */
private val OPEN_SOURCE_NOTICE = """
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
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "开源组件许可",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues: PaddingValues ->
        Text(
            text = OPEN_SOURCE_NOTICE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}
