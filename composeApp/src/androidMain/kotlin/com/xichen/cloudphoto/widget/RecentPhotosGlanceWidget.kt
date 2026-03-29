package com.xichen.cloudphoto.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL

private val snapshotJson = Json { ignoreUnknownKeys = true }

private object WidgetSnapshotFileStore {
    fun read(context: Context): WidgetSnapshotPayload {
        val f = File(context.applicationContext.filesDir, WidgetContract.SNAPSHOT_FILE_NAME)
        if (!f.exists()) {
            return WidgetSnapshotPayload(isLoggedIn = false, items = emptyList(), photoCount = 0)
        }
        return runCatching {
            snapshotJson.decodeFromString(WidgetSnapshotPayload.serializer(), f.readText())
        }.getOrDefault(WidgetSnapshotPayload())
    }
}

/**
 * Home screen widget: recent photo thumbnails. Data: [WidgetContract.SNAPSHOT_FILE_NAME] in files dir.
 */
class RecentPhotosGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetSnapshotFileStore.read(context)
        val bitmaps =
            if (snapshot.isLoggedIn && snapshot.items.isNotEmpty()) {
                snapshot.items.take(4).map { item -> loadThumbnailBitmap(item.imageUrl) }
            } else {
                emptyList()
            }
        provideContent {
            GlanceTheme {
                RecentPhotosWidgetBody(snapshot, bitmaps)
            }
        }
    }
}

class CloudPhotoGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentPhotosGlanceWidget()
}

private suspend fun loadThumbnailBitmap(imageUrl: String): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        URL(imageUrl).openStream().use { BitmapFactory.decodeStream(it) }
    }.getOrNull()
}

@Composable
private fun RecentPhotosWidgetBody(
    snapshot: WidgetSnapshotPayload,
    bitmaps: List<android.graphics.Bitmap?>,
) {
    val context = LocalContext.current
    val openPhotos = actionStartActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(WidgetContract.DEEP_LINK_PHOTOS)).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
    )

    if (!snapshot.isLoggedIn) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.secondaryContainer)
                .padding(16.dp)
                .clickable(openPhotos),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("请登录")
                Text("登录后查看照片")
            }
        }
        return
    }

    if (snapshot.items.isEmpty()) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.secondaryContainer)
                .padding(16.dp)
                .clickable(openPhotos),
            contentAlignment = Alignment.Center,
        ) {
            Text("CloudPhoto · 暂无照片")
        }
        return
    }

    if (snapshot.items.size == 1) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(openPhotos),
        ) {
            bitmaps.getOrNull(0)?.let { bmp ->
                Image(
                    provider = ImageProvider(bmp),
                    contentDescription = snapshot.items[0].name,
                    modifier = GlanceModifier.fillMaxSize(),
                )
            } ?: PlaceholderTile(GlanceModifier.fillMaxSize())
        }
        return
    }

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(openPhotos)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        snapshot.items.take(4).forEachIndexed { index, item ->
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp),
            ) {
                bitmaps.getOrNull(index)?.let { bmp ->
                    Image(
                        provider = ImageProvider(bmp),
                        contentDescription = item.name,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                } ?: PlaceholderTile(GlanceModifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PlaceholderTile(modifier: GlanceModifier) {
    Box(
        modifier = modifier.background(GlanceTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text("·")
    }
}
