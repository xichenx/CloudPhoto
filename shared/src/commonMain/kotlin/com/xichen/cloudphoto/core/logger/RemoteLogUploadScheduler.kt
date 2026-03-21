package com.xichen.cloudphoto.core.logger

import com.xichen.cloudphoto.core.network.ApiResult
import com.xichen.cloudphoto.service.ClientLogApiService
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json

private val decodeJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * 定时将 [RemoteLogFileStore] 中 NDJSON 批量 POST 到服务端；成功则删除已上传行。
 */
object RemoteLogUploadScheduler {

    private const val FLUSH_INTERVAL_MS = 5 * 60_000L
    private const val MAX_LINES_PER_BATCH = 80
    private const val MAX_BATCH_CHARS = 48_000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var periodicJob: Job? = null
    private var api: ClientLogApiService? = null

    fun start(httpClient: HttpClient) {
        if (periodicJob != null) return
        api = ClientLogApiService(httpClient)
        periodicJob = scope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                runCatching { flushOnce() }
            }
        }
    }

    fun stop() {
        periodicJob?.cancel()
        periodicJob = null
        api = null
    }

    fun requestFlushNow() {
        scope.launch {
            runCatching { flushOnce() }
        }
    }

    suspend fun flushPendingNow(): ApiResult<Unit> {
        mutex.lock()
        try {
            return flushLocked()
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun flushOnce() {
        mutex.lock()
        try {
            flushLocked()
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun flushLocked(): ApiResult<Unit> {
        val service = api ?: return ApiResult.Success(Unit)
        val lines = RemoteLogFileStore.peekFirstLines(MAX_LINES_PER_BATCH, MAX_BATCH_CHARS)
        if (lines.isEmpty()) return ApiResult.Success(Unit)
        val entries = lines.mapNotNull { line ->
            runCatching { decodeJson.decodeFromString(ClientLogEntry.serializer(), line) }.getOrNull()
        }
        if (entries.isEmpty()) {
            RemoteLogFileStore.deleteFirstLines(lines.size)
            return ApiResult.Success(Unit)
        }
        return when (val result = service.uploadBatch(entries)) {
            is ApiResult.Success -> {
                RemoteLogFileStore.deleteFirstLines(lines.size)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> ApiResult.Success(Unit)
        }
    }
}
