package com.xichen.cloudphoto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xichen.cloudphoto.AppViewModel
import com.xichen.cloudphoto.core.ResponsiveContainer
import com.xichen.cloudphoto.core.rememberResponsiveConfig
import com.xichen.cloudphoto.navigation.NavGraph
import com.xichen.cloudphoto.navigation.Screen
import com.xichen.cloudphoto.navigation.toAnalyticsPage

/**
 * 底部导航栏占位高度，供各页列表的 contentPadding 使用，避免最后几项被导航栏遮挡。
 * 由 [MainScreen] 提供，列表仅在此底部留白，内容区域不整体缩进。
 */
internal val LocalBottomBarInset = staticCompositionLocalOf { 0.dp }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val responsiveConfig = rememberResponsiveConfig()
    var previousRouteBeforeCamera by remember { mutableStateOf(Screen.Photos.route) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var previousAnalyticsPage by remember { mutableStateOf<String?>(null) }

    ResponsiveContainer {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Photos.route

        val pendingMainTabRoute by viewModel.pendingMainTabRoute.collectAsState()
        LaunchedEffect(pendingMainTabRoute, isLoggedIn) {
            val route = pendingMainTabRoute ?: return@LaunchedEffect
            if (!isLoggedIn) {
                viewModel.clearPendingMainTabRoute()
                return@LaunchedEffect
            }
            viewModel.clearPendingMainTabRoute()
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        LaunchedEffect(currentRoute, isLoggedIn) {
            if (!isLoggedIn) {
                previousAnalyticsPage = null
                return@LaunchedEffect
            }
            val page = currentRoute.toAnalyticsPage()
            viewModel.trackAnalyticsPageView(page, previousAnalyticsPage)
            previousAnalyticsPage = page
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (responsiveConfig.isPhone && currentRoute in Screen.mainTabRoutes) {
                    ModernNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            viewModel.trackBottomNavClick(route, currentRoute)
                            if (route != Screen.Camera.route) {
                                previousRouteBeforeCamera = route
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            val bottomBarInset = paddingValues.calculateBottomPadding()
            androidx.compose.runtime.CompositionLocalProvider(
                LocalBottomBarInset provides bottomBarInset
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        previousRouteBeforeCamera = previousRouteBeforeCamera
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val tabs = listOf(
        TabItem(
            route = Screen.Photos.route,
            icon = Icons.Default.Photo,
            label = "照片",
            isCenter = false
        ),
        TabItem(
            route = Screen.Albums.route,
            icon = Icons.Default.PhotoLibrary,
            label = "相册",
            isCenter = false
        ),
        TabItem(
            route = Screen.Camera.route,
            icon = Icons.Default.CameraAlt,
            label = "拍照",
            isCenter = true
        ),
        TabItem(
            route = Screen.Storage.route,
            icon = Icons.Default.Folder,
            label = "空间",
            isCenter = false
        ),
        TabItem(
            route = Screen.Settings.route,
            icon = Icons.Default.Person,
            label = "我的",
            isCenter = false
        )
    )
    
    val colorScheme = MaterialTheme.colorScheme
    
    // 仅使用导航栏本身高度，不叠加系统 insets，避免底部多出空白遮挡列表
    NavigationBar(
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = currentRoute == tab.route

            if (tab.isCenter) {
                // 中间的拍照按钮 - 大图标，突出显示
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { onNavigate(tab.route) },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (selected) {
                            colorScheme.primary
                        } else {
                            colorScheme.primaryContainer
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = if (selected) 8.dp else 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(28.dp),
                            tint = if (selected) {
                                colorScheme.onPrimary
                            } else {
                                colorScheme.primary
                            }
                        )
                    }
                }
            } else {
                // 普通 Tab - 移除 alpha 动画，使用固定颜色，避免闪烁
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selected,
                    onClick = { onNavigate(tab.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        indicatorColor = colorScheme.primaryContainer.copy(alpha = 0.3f),
                        unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

private data class TabItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val isCenter: Boolean = false
)

