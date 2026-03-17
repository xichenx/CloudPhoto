package com.xichen.cloudphoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * 添加/编辑存储配置 - 独立全屏界面（替代原 Dialog）
 * @param configToEdit 如果提供，则为编辑模式；否则为添加模式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStorageConfigScreen(
    navController: NavController,
    viewModel: AppViewModel,
    configToEdit: StorageConfig? = null
) {
    val isEditMode = configToEdit != null
    
    // 初始化状态：如果是编辑模式，使用已有配置的值
    var name by remember { mutableStateOf(configToEdit?.name ?: "") }
    var provider by remember { mutableStateOf(configToEdit?.provider ?: StorageProvider.ALIYUN_OSS) }
    var endpoint by remember { mutableStateOf(configToEdit?.endpoint ?: "") }
    var accessKeyId by remember { mutableStateOf(configToEdit?.accessKeyId ?: "") }
    var accessKeySecret by remember { mutableStateOf(configToEdit?.accessKeySecret ?: "") }
    var bucketName by remember { mutableStateOf(configToEdit?.bucketName ?: "") }
    var region by remember { mutableStateOf(configToEdit?.region ?: "") }
    var isDefault by remember { mutableStateOf(configToEdit?.isDefault ?: false) }

    fun saveIfValid() {
        if (name.isNotEmpty() && endpoint.isNotEmpty() &&
            accessKeyId.isNotEmpty() && accessKeySecret.isNotEmpty() &&
            bucketName.isNotEmpty()
        ) {
            val config = StorageConfig(
                id = configToEdit?.id ?: "${Clock.System.now().epochSeconds}_${(0..999999).random()}",
                name = name,
                provider = provider,
                endpoint = endpoint,
                accessKeyId = accessKeyId,
                accessKeySecret = accessKeySecret,
                bucketName = bucketName,
                region = region.ifEmpty { null },
                isDefault = isDefault,
                createdAt = configToEdit?.createdAt ?: Clock.System.now().epochSeconds
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
                        if (configToEdit != null) "编辑存储配置" else "添加存储配置",
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
            // 提供商选择（编辑模式下可修改）
            if (!isEditMode) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = provider.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("存储提供商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        StorageProvider.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(getProviderDisplayName(p)) },
                                onClick = {
                                    provider = p
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // 编辑模式下显示提供商（不可修改）
                OutlinedTextField(
                    value = getProviderDisplayName(provider),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("存储提供商") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
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
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = accessKeySecret,
                onValueChange = { accessKeySecret = it },
                label = { Text("Access Key Secret") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                }
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

/**
 * 获取存储提供商的显示名称（中文）
 */
@Composable
private fun getProviderDisplayName(provider: StorageProvider): String {
    return when (provider) {
        StorageProvider.ALIYUN_OSS -> "阿里云 OSS"
        StorageProvider.AWS_S3 -> "AWS S3"
        StorageProvider.TENCENT_COS -> "腾讯云 COS"
        StorageProvider.MINIO -> "MinIO"
        StorageProvider.QINIU -> "七牛云"
        StorageProvider.CUSTOM_S3 -> "自定义 S3"
    }
}
