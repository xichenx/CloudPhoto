package com.xichen.cloudphoto.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 内存 LRU + 同 key 互斥加载；HTTP 层使用 OkHttp [Cache]（有上限、位于 cacheDir，可被系统清理）。
 *
 * 调用 [init] 后才会创建 [HttpClient]；网格建议 [getBitmap] 传入 [maxSidePx] 与缩略图 URL 以降低内存与解码成本。
 */
object PhotoImageCache {

    private const val HTTP_DISK_CACHE_MAX_BYTES = 80L * 1024 * 1024
    private const val CACHE_SUBDIR = "photo_http_cache"

    private val maxBytes = (Runtime.getRuntime().maxMemory() / 8L).toInt().coerceIn(4 * 1024 * 1024, 64 * 1024 * 1024)

    private val memoryCache = object : LruCache<String, Bitmap>(maxBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    @Volatile
    private var client: HttpClient? = null

    private val loadMutexes = ConcurrentHashMap<String, Mutex>()

    fun init(context: Context) {
        if (client != null) return
        synchronized(this) {
            if (client != null) return
            val dir = File(context.applicationContext.cacheDir, CACHE_SUBDIR).apply { mkdirs() }
            client = HttpClient(OkHttp) {
                engine {
                    config {
                        cache(Cache(dir, HTTP_DISK_CACHE_MAX_BYTES))
                    }
                }
            }
        }
    }

    private fun httpClient(): HttpClient =
        client ?: error("PhotoImageCache.init(context) must be called before loading images (e.g. from MainActivity.onCreate)")

    private fun memoryKey(url: String, maxSidePx: Int): String =
        if (maxSidePx <= 0) url else "$url|m$maxSidePx"

    suspend fun getBitmap(url: String, maxSidePx: Int = 0): Bitmap {
        val key = memoryKey(url, maxSidePx)
        memoryCache.get(key)?.let { return it }

        val mutex = loadMutexes.getOrPut(key) { Mutex() }
        return try {
            mutex.withLock {
                memoryCache.get(key)?.let { return@withLock it }
                val bytes = httpClient().get(url).body<ByteArray>()
                val decoded = withContext(Dispatchers.Default) {
                    decodeBitmap(bytes, maxSidePx)
                } ?: error("Bitmap decode failed for url")
                memoryCache.put(key, decoded)
                decoded
            }
        } finally {
            loadMutexes.remove(key, mutex)
        }
    }

    private fun decodeBitmap(bytes: ByteArray, maxSidePx: Int): Bitmap? {
        if (maxSidePx <= 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = inSampleSizeForMaxSide(bounds.outWidth, bounds.outHeight, maxSidePx)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun inSampleSizeForMaxSide(width: Int, height: Int, maxSidePx: Int): Int {
        if (maxSidePx <= 0) return 1
        var sample = 1
        val maxDim = maxOf(width, height)
        while (maxDim / sample > maxSidePx) {
            sample *= 2
        }
        return sample
    }
}
