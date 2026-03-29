package com.xichen.cloudphoto.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.xichen.cloudphoto.R
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xichen.cloudphoto.core.ToastManager
import com.xichen.cloudphoto.core.ToastType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: com.xichen.cloudphoto.AppViewModel? = null
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var account by remember { mutableStateOf("") } // 登录邮箱
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // 监听ViewModel的错误消息和登录状态
    val authError by viewModel?.authError?.collectAsState() ?: remember { mutableStateOf(null) }
    val isLoggedIn by viewModel?.isLoggedIn?.collectAsState() ?: remember { mutableStateOf(false) }
    
    // 当有错误时显示 Toast 并重置加载状态
    LaunchedEffect(authError) {
        authError?.let { message ->
            ToastManager.show(context, message, ToastType.ERROR)
            viewModel?.clearAuthError()
            isLoading = false
        }
    }
    
    // 监听登录状态变化
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            isLoading = false
            onLoginSuccess()
        }
    }
    
    // 现代化渐变背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // 装饰性背景元素
        ModernDecorativeBackground()
        
        // 主内容 - 优化的布局结构
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.12f))
            
            // Logo和标题 - 更精致的设计
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Logo 容器 - 使用启动图标，带阴影
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "应用图标",
                        modifier = Modifier.size(108.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "欢迎回来",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "登录以继续使用云相册",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // 优化的表单卡片 - 更精致的阴影和圆角
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 邮箱登录
                    ModernAuthTextField(
                        value = account,
                        onValueChange = { 
                            account = it
                            accountError = null
                        },
                        label = "邮箱",
                        leadingIcon = Icons.Default.Email,
                        isError = accountError != null,
                        errorMessage = accountError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    
                    // 密码输入
                    ModernAuthTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = "密码",
                        leadingIcon = Icons.Default.Lock,
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                    
                    // 忘记密码链接 - 更紧凑的布局
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { /* TODO: 导航到找回密码 */ },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "忘记密码？",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // 优化的登录按钮 - 更精致的样式
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            accountError = null
                            passwordError = null
                            
                            // 验证
                            var hasError = false
                            
                            if (account.isBlank()) {
                                accountError = "请输入邮箱"
                                hasError = true
                            } else {
                                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                                if (!emailPattern.matcher(account.trim()).matches()) {
                                    accountError = "请输入有效的邮箱地址"
                                    hasError = true
                                }
                            }
                            
                            if (password.isBlank()) {
                                passwordError = "请输入密码"
                                hasError = true
                            }
                            
                            if (hasError) return@Button

                            isLoading = true
                            // 调用ViewModel的登录方法
                            if (viewModel != null) {
                                viewModel.trackLoginSubmit()
                                viewModel.login(account.trim(), password)
                            } else {
                                scope.launch {
                                    delay(1500)
                                    isLoading = false
                                    onLoginSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "登录",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 注册链接
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "还没有账户？",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    viewModel?.trackLoginGoRegister()
                                    onNavigateToRegister()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "立即注册",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}

/**
 * 优化的装饰性背景 - 更精致、更柔和
 */
@Composable
private fun ModernDecorativeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
    ) {
        // 装饰性圆形 - 更柔和的透明度和更精致的渐变
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 380f
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 300.dp, y = 500.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        radius = 330f
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 180.dp, y = 180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        radius = 220f
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * 现代化的认证输入框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    }
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                },
                focusedLabelColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )
        
        // 错误提示
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

