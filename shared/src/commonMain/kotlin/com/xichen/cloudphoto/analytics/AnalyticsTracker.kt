package com.xichen.cloudphoto.analytics

import com.xichen.cloudphoto.concurrent.PlatformMutex
import com.xichen.cloudphoto.core.auth.TokenManager
import com.xichen.cloudphoto.core.logger.Log
import com.xichen.cloudphoto.core.network.onError
import com.xichen.cloudphoto.model.AppEventReportDto
import com.xichen.cloudphoto.service.AppEventApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Fire-and-forget analytics. Events recorded before login are queued (max [MAX_PENDING]) and
 * sent after [flushPending] (call when token is saved).
 */
class AnalyticsTracker(
    private val api: AppEventApiService,
    private val tokenManager: TokenManager,
    private val deviceMeta: AnalyticsDeviceMeta,
    private val scope: CoroutineScope
) {

    private val pendingLock = PlatformMutex()
    private val pending = ArrayDeque<AppEventReportDto>(MAX_PENDING)

    fun pageView(page: String, fromPage: String? = null) {
        enqueue(
            AppEventReportDto(
                eventType = "PAGE_VIEW",
                sessionId = AnalyticsSession.currentId(),
                page = page,
                fromPage = fromPage,
                platform = deviceMeta.platform,
                appVersion = deviceMeta.appVersion,
                osVersion = deviceMeta.osVersion,
                deviceModel = deviceMeta.deviceModel,
                clientTimestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    fun click(
        page: String,
        elementId: String,
        elementType: String,
        elementName: String? = null,
        fromPage: String? = null,
        position: Int? = null,
        extra: String? = null
    ) {
        enqueue(
            AppEventReportDto(
                eventType = "CLICK",
                sessionId = AnalyticsSession.currentId(),
                page = page,
                fromPage = fromPage,
                elementId = elementId,
                elementType = elementType,
                elementName = elementName,
                position = position,
                platform = deviceMeta.platform,
                appVersion = deviceMeta.appVersion,
                osVersion = deviceMeta.osVersion,
                deviceModel = deviceMeta.deviceModel,
                extra = extra,
                clientTimestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    fun exposure(
        page: String,
        elementId: String,
        elementType: String,
        elementName: String? = null,
        position: Int? = null,
        exposureDurationMs: Long,
        extra: String? = null
    ) {
        enqueue(
            AppEventReportDto(
                eventType = "EXPOSURE",
                sessionId = AnalyticsSession.currentId(),
                page = page,
                elementId = elementId,
                elementType = elementType,
                elementName = elementName,
                position = position,
                exposureDurationMs = exposureDurationMs,
                platform = deviceMeta.platform,
                appVersion = deviceMeta.appVersion,
                osVersion = deviceMeta.osVersion,
                deviceModel = deviceMeta.deviceModel,
                extra = extra,
                clientTimestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    private fun enqueue(dto: AppEventReportDto) {
        if (!tokenManager.isLoggedIn()) {
            pendingLock.withLock {
                while (pending.size >= MAX_PENDING) {
                    pending.removeFirst()
                }
                pending.addLast(dto)
            }
            return
        }
        send(dto)
    }

    private fun send(dto: AppEventReportDto) {
        scope.launch {
            api.reportEvent(dto).onError { e, msg ->
                Log.d(TAG, "analytics drop: ${msg ?: e.message}")
            }
        }
    }

    /**
     * Send queued pre-login events; call after access token is persisted.
     */
    fun flushPending() {
        if (!tokenManager.isLoggedIn()) {
            return
        }
        val batch = pendingLock.withLock {
            val list = pending.toList()
            pending.clear()
            list
        }
        for (e in batch) {
            send(e)
        }
    }

    fun clearPending() {
        pendingLock.withLock {
            pending.clear()
        }
    }

    private companion object {
        const val TAG = "Analytics"
        const val MAX_PENDING = 32
    }
}
