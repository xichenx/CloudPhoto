package com.xichen.cloudphoto.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xichen.cloudphoto.ui.AccountSecurityScreen
import com.xichen.cloudphoto.ui.AddStorageConfigScreen
import com.xichen.cloudphoto.ui.ChangePasswordScreen
import com.xichen.cloudphoto.ui.AboutScreen
import com.xichen.cloudphoto.ui.OpenSourceLicensesScreen
import com.xichen.cloudphoto.ui.HelpFeedbackScreen
import com.xichen.cloudphoto.ui.AlbumsScreen
import com.xichen.cloudphoto.ui.CameraScreen
import com.xichen.cloudphoto.ui.PhotosScreen
import com.xichen.cloudphoto.ui.ProfileScreen
import com.xichen.cloudphoto.ui.SettingsScreen
import com.xichen.cloudphoto.ui.NotificationSettingsScreen
import com.xichen.cloudphoto.ui.ThemeSettingsScreen
import com.xichen.cloudphoto.ui.StorageScreen
import com.xichen.cloudphoto.ui.StorageTutorialScreen
import com.xichen.cloudphoto.AppViewModel

/**
 * 路由定义
 */
sealed class Screen(val route: String) {
    object Photos : Screen("photos")
    object Albums : Screen("albums")
    object Camera : Screen("camera")
    object Storage : Screen("storage")
    object AddStorageConfig : Screen("storage/add")
    object EditStorageConfig : Screen("storage/edit/{configId}") {
        fun createRoute(configId: String) = "storage/edit/$configId"
    }
    object StorageTutorial : Screen("storage/tutorial")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object AccountSecurity : Screen("account_security")
    object ChangePassword : Screen("change_password")
    object ThemeSettings : Screen("theme_settings")
    object NotificationSettings : Screen("notification_settings")
    object HelpFeedback : Screen("help_feedback")
    object About : Screen("about")
    object OpenSourceLicenses : Screen("open_source_licenses")

    /**
     * 属于底部导航栏的主 Tab 路由。仅在这些界面显示底部栏；
     * 其他界面（如添加配置）以全屏方式展示，类似 Activity 跳转。
     * 使用字面量避免 companion 初始化时引用未初始化的嵌套 object 导致 NPE。
     */
    companion object {
        val mainTabRoutes: Set<String> = setOf(
            "photos",
            "albums",
            "camera",
            "storage",
            "settings"
        )
    }
}

// 转场时长略延长，视觉更顺滑
private const val NAV_ANIM_DURATION = 320
// Material 标准曲线：先快后慢，收尾更自然
private val NAV_EASING = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
// 滑动距离为 70% 宽度，减少位移感、提升流畅度
private const val SLIDE_FRACTION = 0.7f

private fun navTween() = tween<Float>(
    durationMillis = NAV_ANIM_DURATION,
    easing = NAV_EASING
)

private fun navSlideTween() = tween<IntOffset>(
    durationMillis = NAV_ANIM_DURATION,
    easing = NAV_EASING
)

/**
 * 导航图 - 主界面导航（不包含登录界面）
 *
 * 使用滑动 + 淡入淡出实现流畅的页面切换（类似 Activity 转场）。
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: AppViewModel,
    previousRouteBeforeCamera: String = Screen.Photos.route,
    startDestination: String = Screen.Photos.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { (it * SLIDE_FRACTION).toInt() },
                animationSpec = navSlideTween()
            ) + fadeIn(animationSpec = navTween())
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (-it * SLIDE_FRACTION).toInt() },
                animationSpec = navSlideTween()
            ) + fadeOut(animationSpec = navTween())
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { (-it * SLIDE_FRACTION).toInt() },
                animationSpec = navSlideTween()
            ) + fadeIn(animationSpec = navTween())
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * SLIDE_FRACTION).toInt() },
                animationSpec = navSlideTween()
            ) + fadeOut(animationSpec = navTween())
        }
    ) {
        composable(Screen.Photos.route) {
            PhotosScreen(viewModel = viewModel)
        }
        composable(Screen.Albums.route) {
            AlbumsScreen(viewModel = viewModel)
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel,
                onCameraDismissed = {
                    navController.navigate(previousRouteBeforeCamera) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Storage.route) {
            StorageScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.AddStorageConfig.route) {
            AddStorageConfigScreen(
                navController = navController,
                viewModel = viewModel,
                configToEdit = null
            )
        }
        composable(
            route = Screen.EditStorageConfig.route,
            arguments = listOf(
                navArgument("configId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId") ?: ""
            val configToEdit = viewModel.configs.value.firstOrNull { it.id == configId }
            AddStorageConfigScreen(
                navController = navController,
                viewModel = viewModel,
                configToEdit = configToEdit
            )
        }
        composable(Screen.StorageTutorial.route) {
            StorageTutorialScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AccountSecurity.route) {
            AccountSecurityScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ThemeSettings.route) {
            ThemeSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.HelpFeedback.route) {
            HelpFeedbackScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OpenSourceLicenses.route) {
            OpenSourceLicensesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

