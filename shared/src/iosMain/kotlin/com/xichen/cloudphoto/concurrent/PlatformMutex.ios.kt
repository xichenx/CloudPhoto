package com.xichen.cloudphoto.concurrent

import platform.Foundation.NSLock

internal actual class PlatformMutex {
    private val lock = NSLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
