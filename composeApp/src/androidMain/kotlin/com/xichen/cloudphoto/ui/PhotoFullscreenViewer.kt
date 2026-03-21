package com.xichen.cloudphoto.ui

import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.xichen.cloudphoto.model.Photo
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.Offset

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f
private const val ZOOMED_THRESHOLD = 1.02f

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ZoomableCoilImage(
            imageUrl = photo.url,
            contentDescription = photo.name,
            onZoomedChange = onZoomedChange
        )
    }
}

@Composable
private fun ZoomableCoilImage(
    imageUrl: String,
    contentDescription: String?,
    onZoomedChange: (Boolean) -> Unit
) {
    val scaleState = remember(imageUrl) { mutableFloatStateOf(1f) }
    val offsetState = remember(imageUrl) { mutableStateOf(Offset.Zero) }
    val onZoomedUpdated = rememberUpdatedState(onZoomedChange)
    val context = LocalContext.current
    val request = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(300)
            .build()
    }

    val pinchDetector = remember(imageUrl, context) {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val next = (scaleState.floatValue * detector.scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)
                    scaleState.floatValue = next
                    if (next <= ZOOMED_THRESHOLD) {
                        offsetState.value = Offset.Zero
                    }
                    onZoomedUpdated.value(next > ZOOMED_THRESHOLD)
                    return true
                }
            }
        )
    }

    val isZoomed = scaleState.floatValue > ZOOMED_THRESHOLD

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter(
                onTouchEvent = { event: MotionEvent ->
                    if (scaleState.floatValue > ZOOMED_THRESHOLD) {
                        false
                    } else {
                        // 必须返回 onTouchEvent 的布尔值；用 isInProgress 会在缩放手势成立前一直为 false，双指序列会断掉
                        pinchDetector.onTouchEvent(event)
                    }
                }
            )
            .pointerInput(isZoomed, imageUrl) {
                if (isZoomed) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scaleState.floatValue * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                        scaleState.floatValue = newScale
                        offsetState.value = offsetState.value + pan
                        if (newScale <= ZOOMED_THRESHOLD) {
                            offsetState.value = Offset.Zero
                        }
                        onZoomedUpdated.value(newScale > ZOOMED_THRESHOLD)
                    }
                }
            }
            .pointerInput(imageUrl) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scaleState.floatValue > ZOOMED_THRESHOLD) {
                            scaleState.floatValue = 1f
                            offsetState.value = Offset.Zero
                            onZoomedUpdated.value(false)
                        } else {
                            scaleState.floatValue = 2.5f.coerceIn(MIN_SCALE, MAX_SCALE)
                            onZoomedUpdated.value(true)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scaleState.floatValue,
                    scaleY = scaleState.floatValue,
                    translationX = offsetState.value.x,
                    translationY = offsetState.value.y
                ),
            contentScale = ContentScale.Fit
        ) {
            when (painter.state) {
                AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading -> {
                    ShimmerPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                        baseColor = Color(0xFF161616),
                        isOnDarkBackground = true
                    )
                }
                is AsyncImagePainter.State.Error -> {
                    Text(
                        text = "无法加载图片",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is AsyncImagePainter.State.Success -> {
                    SubcomposeAsyncImageContent()
                }
            }
        }
    }
}
