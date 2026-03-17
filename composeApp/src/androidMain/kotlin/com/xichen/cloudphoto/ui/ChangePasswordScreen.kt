package com.xichen.cloudphoto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xichen.cloudphoto.core.ToastManager
import com.xichen.cloudphoto.core.ToastType
import com.xichen.cloudphoto.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 修改密码独立界面：先验证码验证，再输入旧密码与新密码修改
 *
 * @param viewModel 应用 ViewModel
 * @param onBack 返回或完成后回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val changePasswordSuccess by viewModel.changePasswordSuccess.collectAsState()

    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf(currentUser?.email?.orEmpty() ?: "") }
    var emailCode by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(0) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (email.isEmpty() && currentUser?.email != null) email = currentUser!!.email!!
    }

    LaunchedEffect(authError) {
        authError?.let { msg ->
            ToastManager.show(context, msg, ToastType.ERROR)
            viewModel.clearAuthError()
            isLoading = false
        }
    }

    LaunchedEffect(changePasswordSuccess) {
        if (changePasswordSuccess) {
            viewModel.clearChangePasswordSuccess()
            ToastManager.show(context, "密码修改成功", ToastType.SUCCESS)
            onBack()
        }
    }

    LaunchedEffect(countdown) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (step == 1) "验证身份" else "设置新密码",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
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
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            if (step == 1) {
                Text(
                    text = "我们将向您的邮箱发送验证码，请先完成验证",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = !currentUser?.email.isNullOrEmpty()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = emailCode,
                        onValueChange = { emailCode = it },
                        label = { Text("验证码") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                ToastManager.show(context, "请先输入邮箱", ToastType.WARNING)
                                return@Button
                            }
                            viewModel.sendEmailCode(email, "reset")
                            scope.launch {
                                countdown = 60
                            }
                            ToastManager.show(context, "验证码已发送，请查收邮箱", ToastType.SUCCESS)
                        },
                        enabled = countdown == 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(if (countdown > 0) "${countdown}s" else "获取验证码")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (emailCode.isBlank()) {
                            ToastManager.show(context, "请输入验证码", ToastType.WARNING)
                            return@Button
                        }
                        step = 2
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("下一步")
                }
            } else {
                Text(
                    text = "请输入当前密码并设置新密码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)
                )
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("当前密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (newPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        when {
                            oldPassword.isBlank() -> ToastManager.show(context, "请输入当前密码", ToastType.WARNING)
                            newPassword.isBlank() -> ToastManager.show(context, "请输入新密码", ToastType.WARNING)
                            newPassword != confirmPassword -> ToastManager.show(context, "两次输入的新密码不一致", ToastType.WARNING)
                            newPassword.length < 6 -> ToastManager.show(context, "新密码至少 6 位", ToastType.WARNING)
                            else -> {
                                isLoading = true
                                viewModel.changePassword(email, emailCode, oldPassword, newPassword)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("完成")
                    }
                }
                TextButton(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("返回上一步")
                }
            }
        }
    }
}
