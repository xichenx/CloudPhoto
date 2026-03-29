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
 * 用户主动上传诊断日志的结果（供 Android / iOS 展示文案）。
 *
 * @property code `0` 已上传至少一批；`1` 队列空；`2` 失败（见 [message]）。
 */
data class UploadDiagnosticLogsOutcome(
    val code: Int,
    val message: String? = null,
) {
    companion object {
        const val CODE_UPLOADED: Int = 0
        const val CODE_NO_PENDING: Int = 1
        const val CODE_ERROR: Int = 2
    }
}

/**
 * 将 [RemoteLogFileStore] 中 NDJSON 批量 POST 到服务端；成功则删除已上传行。
 * 默认定时上传由 [RemoteLogConfig.periodicRemoteUploadEnabled] 控制；亦可 [uploadDiagnosticLogsNow] 主动上传。
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
        api = ClientLogApiService(httpClient)
        if (!RemoteLogConfig.periodicRemoteUploadEnabled) return
        if (periodicJob != null) return
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

    /**
     * 在单锁内连续上传直至队列为空或失败（用于「关于 → 上传日志」）。
     */
    suspend fun uploadDiagnosticLogsNow(): UploadDiagnosticLogsOutcome {
        mutex.lock()
        try {
            var uploadedAnyBatch = false
            while (true) {
                val lines = RemoteLogFileStore.peekFirstLines(MAX_LINES_PER_BATCH, MAX_BATCH_CHARS)
                if (lines.isEmpty()) {
                    return if (uploadedAnyBatch) {
                        UploadDiagnosticLogsOutcome(UploadDiagnosticLogsOutcome.CODE_UPLOADED)
                    } else {
                        UploadDiagnosticLogsOutcome(UploadDiagnosticLogsOutcome.CODE_NO_PENDING)
                    }
                }
                when (val result = flushLocked()) {
                    is ApiResult.Success -> uploadedAnyBatch = true
                    is ApiResult.Error -> {
                        val msg = result.message ?: result.exception.message ?: "上传失败"
                        return UploadDiagnosticLogsOutcome(UploadDiagnosticLogsOutcome.CODE_ERROR, msg)
                    }
                    is ApiResult.Loading -> {
                        return UploadDiagnosticLogsOutcome(UploadDiagnosticLogsOutcome.CODE_UPLOADED)
                    }
                }
            }
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
