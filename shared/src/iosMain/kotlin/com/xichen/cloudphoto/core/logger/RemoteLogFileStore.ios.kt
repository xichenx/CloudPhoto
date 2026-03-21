package com.xichen.cloudphoto.core.logger

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLock
import platform.Foundation.NSUserDomainMask
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fflush
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite

private const val MAX_FILE_BYTES = 512 * 1024

@OptIn(ExperimentalForeignApi::class)
internal actual object RemoteLogFileStore {
    private val fileLock = NSLock()
    private var path: String? = null

    actual fun init(rootContext: Any?) {
        fileLock.lock()
        try {
            val dir = NSFileManager.defaultManager.URLForDirectory(
                directory = NSCachesDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null
            )?.path
            if (dir != null) {
                path = "$dir/cloudphoto_remote_logs.ndjson"
            }
        } finally {
            fileLock.unlock()
        }
    }

    actual fun appendLine(line: String) {
        val p = path ?: return
        fileLock.lock()
        try {
            trimIfNeeded(p)
            val existing = readUtf8File(p)
            writeUtf8File(p, existing + line + "\n")
        } finally {
            fileLock.unlock()
        }
    }

    private fun trimIfNeeded(p: String) {
        val text = readUtf8File(p)
        if (text.length <= MAX_FILE_BYTES) return
        val tail = text.takeLast(MAX_FILE_BYTES / 2)
        writeUtf8File(p, tail)
    }

    actual fun peekFirstLines(maxLines: Int, maxTotalChars: Int): List<String> {
        val p = path ?: return emptyList()
        fileLock.lock()
        try {
            val text = readUtf8File(p)
            if (text.isBlank()) return emptyList()
            val lines = text.lines()
            val result = mutableListOf<String>()
            var used = 0
            for (line in lines) {
                if (line.isBlank()) continue
                if (result.size >= maxLines) break
                val addLen = line.length + 1
                if (used + addLen > maxTotalChars && result.isNotEmpty()) break
                result.add(line)
                used += addLen
            }
            return result
        } finally {
            fileLock.unlock()
        }
    }

    actual fun deleteFirstLines(count: Int) {
        val p = path ?: return
        if (count <= 0) return
        fileLock.lock()
        try {
            val text = readUtf8File(p)
            val lines = text.lines().filter { it.isNotBlank() }
            if (count >= lines.size) {
                NSFileManager.defaultManager.removeItemAtPath(p, error = null)
                return
            }
            val remaining = lines.drop(count).joinToString("\n") + "\n"
            writeUtf8File(p, remaining)
        } finally {
            fileLock.unlock()
        }
    }

    actual fun queuedLineCount(): Int {
        val p = path ?: return 0
        fileLock.lock()
        try {
            val text = readUtf8File(p)
            return text.lines().count { it.isNotBlank() }
        } finally {
            fileLock.unlock()
        }
    }

    private fun readUtf8File(p: String): String {
        val f = fopen(p, "rb") ?: return ""
        try {
            fseek(f, 0, SEEK_END)
            val sz = ftell(f)
            if (sz <= 0L) return ""
            fseek(f, 0, SEEK_SET)
            val buf = ByteArray(sz.toInt())
            buf.usePinned { pinned ->
                fread(pinned.addressOf(0), 1u, sz.convert(), f)
            }
            return buf.decodeToString()
        } finally {
            fclose(f)
        }
    }

    private fun writeUtf8File(p: String, content: String) {
        val bytes = content.encodeToByteArray()
        val f = fopen(p, "wb") ?: return
        try {
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1u, bytes.size.convert(), f)
            }
            fflush(f)
        } finally {
            fclose(f)
        }
    }
}
