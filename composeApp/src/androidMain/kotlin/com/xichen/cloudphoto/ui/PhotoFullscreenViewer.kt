package com.xichen.cloudphoto.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xichen.cloudphoto.cache.PhotoImageCache
import com.xichen.cloudphoto.model.Photo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Offset

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f

/**
 * 全屏照片查看：左右滑动切换、双指缩放、双击放大/还原，行为接近系统相册。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoFullscreenViewer(
    photos: List<Photo>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    if (photos.isEmpty()) return

    val safeIndex = initialIndex.coerceIn(0, photos.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = safeIndex,
        pageCount = { photos.size }
    )
    var pagerScrollEnabled by remember { mutableStateOf(true) }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(pagerState.currentPage) {
        pagerScrollEnabled = true
    }

    DisposableEffect(Unit) {
        systemUiController.setStatusBarColor(Color.Black, darkIcons = false)
        systemUiController.setNavigationBarColor(Color.Black, darkIcons = false)
        onDispose {
            systemUiController.setStatusBarColor(Color.Transparent, darkIcons = true)
            systemUiController.setNavigationBarColor(Color.Transparent, darkIcons = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = pagerScrollEnabled,
                    beyondViewportPageCount = 1
                ) { page ->
                    FullscreenPhotoPage(
                        photo = photos[page],
                        onZoomedChange = { zoomed ->
                            if (page == pagerState.currentPage) {
                                pagerScrollEnabled = !zoomed
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${pagerState.currentPage + 1} / ${photos.size}",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun FullscreenPhotoPage(
    photo: Photo,
    onZoomedChange: (Boolean) -> Unit
) {
    var bitmap by remember(photo.id) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var loading by remember(photo.id) { mutableStateOf(true) }

    LaunchedEffect(photo.id, photo.url) {
        loading = true
        bitmap = null
        try {
            bitmap = PhotoImageCache.getBitmap(photo.url)
        } catch (_: Exception) {
            bitmap = null
        } finally {
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> {
                ShimmerPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                    baseColor = Color(0xFF161616),
                    isOnDarkBackground = true
                )
            }
            bitmap != null -> {
                ZoomableBitmapImage(
                    bitmap = bitmap!!,
                    contentDescription = photo.name,
                    onZoomedChange = onZoomedChange
                )
            }
            else -> {
                Text(
                    text = "无法加载图片",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ZoomableBitmapImage(
    bitmap: android.graphics.Bitmap,
    contentDescription: String?,
    onZoomedChange: (Boolean) -> Unit
) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }

    fun notifyZoomed() {
        onZoomedChange(scale > 1.02f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(bitmap) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.02f) {
                            scale = 1f
                            offset = Offset.Zero
                            onZoomedChange(false)
                        } else {
                            scale = 2.5f.coerceIn(MIN_SCALE, MAX_SCALE)
                            onZoomedChange(true)
                        }
                    }
                )
            }
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                    scale = newScale
                    offset = offset + pan
                    notifyZoomed()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
