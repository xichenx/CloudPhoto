package com.xichen.cloudphoto

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.xichen.cloudphoto.core.ResponsiveContainer
import com.xichen.cloudphoto.theme.CloudPhotoTheme
import com.xichen.cloudphoto.navigation.AuthNavGraph
import com.xichen.cloudphoto.ui.MainScreen

@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    
    // 调试日志
    LaunchedEffect(isLoggedIn) {
        android.util.Log.d("App", "isLoggedIn state changed: $isLoggedIn")
    }
    
    // 在外部创建 NavController，避免在 AnimatedContent 内部重新创建
    val authNavController = rememberNavController()
    
    ResponsiveContainer {
        CloudPhotoTheme {
            // 根据登录状态显示不同的界面，添加平滑过渡动画
            AnimatedContent(
                targetState = isLoggedIn,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "auth_to_main_transition"
            ) { loggedIn ->
                if (loggedIn) {
                    // 已登录，显示主界面
                    MainScreen(viewModel = viewModel)
                } else {
                    // 未登录，显示认证导航（登录/注册）
                    AuthNavGraph(
                        navController = authNavController,
                        viewModel = viewModel,
                        onLoginSuccess = {
                            // 登录成功后的处理在ViewModel中完成，界面会自动切换
                            // ViewModel 会更新 isLoggedIn 状态，AnimatedContent 会自动切换到主界面
                        }
                    )
                }
            }
        }
    }
}