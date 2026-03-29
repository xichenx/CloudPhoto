package com.xichen.cloudphoto.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xichen.cloudphoto.AppViewModel
import com.xichen.cloudphoto.analytics.AnalyticsEventIds
import com.xichen.cloudphoto.analytics.AnalyticsPages
import com.xichen.cloudphoto.core.ToastType
import com.xichen.cloudphoto.core.config.ApiConfig
import com.xichen.cloudphoto.core.rememberToast
import com.xichen.cloudphoto.help.HelpFeedbackCopy

private data class FeedbackCategoryOption(
    val label: String,
    val value: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val showToast = rememberToast()
    val submitting by viewModel.feedbackSubmitting.collectAsState()
    val success by viewModel.feedbackSuccess.collectAsState()
    val error by viewModel.feedbackError.collectAsState()

    var contentText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("general") }

    val categories = remember {
        listOf(
            FeedbackCategoryOption("问题反馈", "bug"),
            FeedbackCategoryOption("功能建议", "suggestion"),
            FeedbackCategoryOption("其他", "general"),
        )
    }
    val mainScroll = rememberScrollState()
    val categoryScroll = rememberScrollState()

    LaunchedEffect(success) {
        if (success) {
            showToast("感谢反馈，我们已收到", ToastType.SUCCESS)
            contentText = ""
            viewModel.clearFeedbackUiState()
        }
    }

    LaunchedEffect(error) {
        val msg = error ?: return@LaunchedEffect
        showToast(msg, ToastType.ERROR)
        viewModel.clearFeedbackUiState()
    }

    fun openTutorialInBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ApiConfig.STORAGE_CONFIG_TUTORIAL_URL))
        runCatching { context.startActivity(Intent.createChooser(intent, "打开链接")) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "帮助与反馈",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(mainScroll)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "常见问题",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            HelpFeedbackCopy.faqItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = item.question,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = item.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { openTutorialInBrowser() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("查看存储配置教程（网页）")
            }

            Text(
                text = "提交反馈",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "分类",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { opt ->
                    FilterChip(
                        selected = category == opt.value,
                        onClick = { category = opt.value },
                        label = { Text(opt.label) },
                    )
                }
            }

            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 12,
                label = { Text("反馈内容（必填，5～2000 字）") },
                placeholder = { Text("请描述问题或建议…") },
                shape = RoundedCornerShape(12.dp),
            )

            Button(
                onClick = {
                    viewModel.trackClick(
                        page = AnalyticsPages.HELP_FEEDBACK,
                        eventId = AnalyticsEventIds.FEEDBACK_SUBMIT,
                        elementType = "button",
                        elementName = "提交反馈",
                    )
                    viewModel.submitFeedback(contentText, category)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 24.dp),
                enabled = !submitting,
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("提交反馈")
                }
            }
        }
    }
}
