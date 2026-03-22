package com.xichen.cloudphoto.analytics

import kotlin.random.Random

/**
 * In-memory session id for funnel and path analysis; refresh after login / explicit reset.
 */
object AnalyticsSession {
    private var sessionId: String = newSessionId()

    fun currentId(): String = sessionId

    fun refresh() {
        sessionId = newSessionId()
    }

    private fun newSessionId(): String =
        buildString(24) {
            append("sess_")
            repeat(20) { append(Random.nextInt(0, 16).toString(16)) }
        }
}
