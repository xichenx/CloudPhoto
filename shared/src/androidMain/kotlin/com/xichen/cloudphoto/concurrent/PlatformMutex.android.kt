package com.xichen.cloudphoto.concurrent

internal actual class PlatformMutex {
    private val lock = Any()

    actual fun <T> withLock(block: () -> T): T = synchronized(lock, block)
}
