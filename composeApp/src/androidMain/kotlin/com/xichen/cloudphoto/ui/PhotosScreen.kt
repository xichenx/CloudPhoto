package com.xichen.cloudphoto.ui

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.xichen.cloudphoto.AppViewModel
import com.xichen.cloudphoto.model.Photo
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(viewModel: AppViewModel) {
    val photos by viewModel.photos.collectAsState()
    var fullscreenStartIndex by remember { mutableStateOf<Int?>(null) }

    fullscreenStartIndex?.let { index ->
        PhotoFullscreenViewer(
            photos = photos,
            initialIndex = index,
            onDismiss = { fullscreenStartIndex = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "照片",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { /* TODO: 搜索功能 */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            // 现代化的空状态
            EmptyPhotosState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // 优化的照片网格 - 参考 Google Photos 设计
            // 更小的间距（2dp），更流畅的视觉效果
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 2.dp,
                    end = 2.dp,
                    top = paddingValues.calculateTopPadding() + 4.dp,
                    bottom = LocalBottomBarInset.current + 4.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = photos,
                    key = { _, photo -> photo.id }
                ) { index, photo ->
                    ModernPhotoItem(
                        photo = photo,
                        viewModel = viewModel,
                        onClick = { fullscreenStartIndex = index }
                    )
                }
            }
        }
    }
}

/**
 * 优化的空状态 - 参考 Google Photos 和 Apple Photos 设计
 * 
 * 设计特点：
 * - 更柔和的渐变背景
 * - 更大的图标和更好的视觉层次
 * - 更友好的引导文案
 * - 优雅的动画效果
 */
@Composable
private fun EmptyPhotosState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            // 优化的图标设计 - 更大的尺寸，更柔和的渐变
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            
            // 优化的文案设计
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "还没有云端照片",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "前往“拍照”页面拍摄并上传\n您的第一张照片到云端",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}

/**
 * 现代化的照片项
 */
@Composable
fun ModernPhotoItem(
    photo: Photo,
    viewModel: AppViewModel,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val gridMaxSidePx = remember(configuration.screenWidthDp, density.density) {
        (configuration.screenWidthDp * density.density / 3f).toInt().coerceIn(64, 2048)
    }
    val imageUrl = remember(photo.id, photo.thumbnailUrl, photo.url) {
        photo.thumbnailUrl?.takeIf { it.isNotBlank() } ?: photo.url
    }
    val request = remember(imageUrl, gridMaxSidePx) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(gridMaxSidePx, gridMaxSidePx))
            .crossfade(200)
            .build()
    }

    // Material 3 风格的照片卡片 - 无边框，圆角更小，参考 Google Photos
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            SubcomposeAsyncImage(
                model = request,
                contentDescription = photo.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            ) {
                when (painter.state) {
                    AsyncImagePainter.State.Empty,
                    is AsyncImagePainter.State.Loading -> {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
                    }
                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }
                }
            }
        }
    }
}

@Composable
fun UploadDialog(
    onDismiss: () -> Unit,
    onUpload: (ByteArray, String, String, Int, Int) -> Unit,
    context: Context
) {
    val photoFile = remember { File(context.getExternalFilesDir(null), "temp_photo.jpg") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("上传照片") },
        text = { Text("是否上传这张照片到云端？") },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        if (photoFile.exists()) {
                            val bytes = withContext(Dispatchers.IO) {
                                photoFile.readBytes()
                            }
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(photoFile.absolutePath, options)
                            onUpload(
                                bytes,
                                photoFile.name,
                                "image/jpeg",
                                options.outWidth,
                                options.outHeight
                            )
                            withContext(Dispatchers.IO) {
                                photoFile.delete()
                            }
                        }
                    }
                }
            ) {
                Text("上传")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

