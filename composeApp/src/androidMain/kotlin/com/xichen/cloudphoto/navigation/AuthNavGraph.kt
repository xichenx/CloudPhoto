package com.xichen.cloudphoto.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xichen.cloudphoto.ui.LoginScreen
import com.xichen.cloudphoto.ui.RegisterScreen
import com.xichen.cloudphoto.AppViewModel

/**
 * 认证路由定义
 */
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
}

/**
 * 认证导航图 - 处理登录和注册之间的导航（无动画）
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    viewModel: AppViewModel?,
    onLoginSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = {
                    navController.navigate(AuthScreen.Register.route) {
                        popUpTo(AuthScreen.Login.route)
                    }
                },
                viewModel = viewModel
            )
        }
        
        composable(AuthScreen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = onLoginSuccess,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}

