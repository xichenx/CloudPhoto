package com.xichen.cloudphoto.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xichen.cloudphoto.ui.AddStorageConfigScreen
import com.xichen.cloudphoto.ui.AlbumsScreen
import com.xichen.cloudphoto.ui.CameraScreen
import com.xichen.cloudphoto.ui.PhotosScreen
import com.xichen.cloudphoto.ui.SettingsScreen
import com.xichen.cloudphoto.ui.StorageScreen
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
    object Settings : Screen("settings")

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

private const val NAV_ANIM_DURATION = 300

private fun navTween() = tween<Float>(
    durationMillis = NAV_ANIM_DURATION,
    easing = FastOutSlowInEasing
)

private fun navSlideTween() = tween<IntOffset>(
    durationMillis = NAV_ANIM_DURATION,
    easing = FastOutSlowInEasing
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
            slideInHorizontally(initialOffsetX = { it }, animationSpec = navSlideTween()) +
                fadeIn(animationSpec = navTween())
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = navSlideTween()) +
                fadeOut(animationSpec = navTween())
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = navSlideTween()) +
                fadeIn(animationSpec = navTween())
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = navSlideTween()) +
                fadeOut(animationSpec = navTween())
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
                viewModel = viewModel
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
    }
}

