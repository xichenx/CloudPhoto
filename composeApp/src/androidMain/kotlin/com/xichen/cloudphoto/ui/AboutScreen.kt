package com.xichen.cloudphoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.xichen.cloudphoto.AppViewModel
import com.xichen.cloudphoto.BuildConfig
import com.xichen.cloudphoto.analytics.AnalyticsEventIds
import com.xichen.cloudphoto.analytics.AnalyticsPages
import com.xichen.cloudphoto.core.ToastType
import com.xichen.cloudphoto.core.rememberToast
import com.xichen.cloudphoto.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AppViewModel,
    navController: NavHostController,
    onBack: () -> Unit,
) {
    val showToast = rememberToast()
    val logUploadUi by viewModel.logUploadUi.collectAsState()
    val diagnosticLogUploading by viewModel.diagnosticLogUploading.collectAsState()
    val scroll = rememberScrollState()
    var showUploadLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logUploadUi) {
        val ev = logUploadUi ?: return@LaunchedEffect
        showToast(ev.message, ev.type)
        viewModel.clearLogUploadUi()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "关于",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "CloudPhoto",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "云相册",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            ModernSettingsCell(
                title = "开源组件许可",
                subtitle = "查看本应用使用的主要开源软件及许可证说明",
                icon = Icons.Default.Description,
                onClick = {
                    viewModel.trackClick(
                        page = AnalyticsPages.ABOUT,
                        eventId = AnalyticsEventIds.ABOUT_OPEN_SOURCE_LICENSES,
                        elementType = "list_item",
                        elementName = "开源组件许可",
                    )
                    navController.navigate(Screen.OpenSourceLicenses.route)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            ModernSettingsCell(
                title = if (diagnosticLogUploading) "正在上传…" else "上传日志",
                subtitle = "将本机缓存的诊断日志上传到服务器（需登录）",
                icon = Icons.Default.Upload,
                enabled = !diagnosticLogUploading,
                onClick = { showUploadLogDialog = true },
                modifier = Modifier.fillMaxWidth(),
            )

            ModernSettingsCell(
                title = "当前版本",
                icon = Icons.Default.Info,
                onClick = {},
                readOnly = true,
                trailingValue = BuildConfig.VERSION_NAME,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showUploadLogDialog) {
        AlertDialog(
            onDismissRequest = { showUploadLogDialog = false },
            title = {
                Text(
                    text = "上传诊断日志？",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Text(
                    text = "将本机缓存的诊断日志上传到服务器，用于问题排查。需登录账号。",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.trackClick(
                            page = AnalyticsPages.ABOUT,
                            eventId = AnalyticsEventIds.ABOUT_UPLOAD_LOGS,
                            elementType = "button",
                            elementName = "确认上传日志",
                        )
                        viewModel.uploadDiagnosticLogsNow()
                        showUploadLogDialog = false
                    },
                ) {
                    Text("上传", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadLogDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}
