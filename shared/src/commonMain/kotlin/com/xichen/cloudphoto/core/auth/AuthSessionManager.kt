package com.xichen.cloudphoto.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide auth events (Flow-based).
 *
 * Network layer emits Unauthorized when token is expired/invalid (401/403),
 * UI layer collects and clears local session to navigate back to login.
 */
object AuthEvents {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun unauthorized(message: String? = null) {
        _events.tryEmit(AuthEvent.Unauthorized(message))
    }
}

sealed class AuthEvent {
    data class Unauthorized(val message: String?) : AuthEvent()
}

