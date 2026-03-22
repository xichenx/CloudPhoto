package com.xichen.cloudphoto.concurrent

/**
 * Cross-platform mutex for short critical sections (JVM: synchronized; Apple: NSLock).
 */
internal expect class PlatformMutex() {
    fun <T> withLock(block: () -> T): T
}
