package com.xichen.cloudphoto.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.xichen.cloudphoto.AppViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * 拍照 Screen - 与 iOS 一致：点击拍照 Tab 直接打开相机，无占位页，关闭后返回上一个 Tab
 */
@Composable
fun CameraScreen(
    viewModel: AppViewModel,
    onCameraDismissed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var hasLaunchedPermission by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            showUploadDialog = true
        } else {
            onCameraDismissed()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.getExternalFilesDir(null), "temp_photo.jpg")
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } else {
            onCameraDismissed()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLaunchedPermission) {
            hasLaunchedPermission = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showUploadDialog) {
            UploadDialog(
                onDismiss = {
                    showUploadDialog = false
                    onCameraDismissed()
                },
                onUpload = { photoData, fileName, mimeType, width, height ->
                    scope.launch {
                        uploading = true
                        viewModel.uploadPhoto(photoData, fileName, mimeType, width, height)
                        uploading = false
                        showUploadDialog = false
                        onCameraDismissed()
                    }
                },
                context = context
            )
        }
    }
}
