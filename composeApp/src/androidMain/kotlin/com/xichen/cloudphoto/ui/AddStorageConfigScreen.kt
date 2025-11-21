package com.xichen.cloudphoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xichen.cloudphoto.AppViewModel
import com.xichen.cloudphoto.model.StorageConfig
import com.xichen.cloudphoto.model.StorageProvider
import kotlinx.datetime.Clock

/**
 * 添加存储配置 - 独立全屏界面（替代原 Dialog）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStorageConfigScreen(
    navController: NavController,
    viewModel: AppViewModel
) {
    var name by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(StorageProvider.ALIYUN_OSS) }
    var endpoint by remember { mutableStateOf("") }
    var accessKeyId by remember { mutableStateOf("") }
    var accessKeySecret by remember { mutableStateOf("") }
    var bucketName by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    fun saveIfValid() {
        if (name.isNotEmpty() && endpoint.isNotEmpty() &&
            accessKeyId.isNotEmpty() && accessKeySecret.isNotEmpty() &&
            bucketName.isNotEmpty()
        ) {
            val config = StorageConfig(
                id = "${Clock.System.now().epochSeconds}_${(0..999999).random()}",
                name = name,
                provider = provider,
                endpoint = endpoint,
                accessKeyId = accessKeyId,
                accessKeySecret = accessKeySecret,
                bucketName = bucketName,
                region = region.ifEmpty { null },
                isDefault = isDefault,
                createdAt = Clock.System.now().epochSeconds
            )
            viewModel.saveConfig(config)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "添加存储配置",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveIfValid() }
                    ) {
                        Text(
                            "保存",
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("配置名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                label = { Text("Endpoint") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = accessKeyId,
                onValueChange = { accessKeyId = it },
                label = { Text("Access Key ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = accessKeySecret,
                onValueChange = { accessKeySecret = it },
                label = { Text("Access Key Secret") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = bucketName,
                onValueChange = { bucketName = it },
                label = { Text("Bucket Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Region (可选)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
                Text(
                    "设为默认",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { saveIfValid() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存配置")
            }
        }
    }
}
