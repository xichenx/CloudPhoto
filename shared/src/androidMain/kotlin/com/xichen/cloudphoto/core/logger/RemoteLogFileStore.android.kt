package com.xichen.cloudphoto.core.logger

import android.content.Context
import java.io.File

private const val MAX_FILE_BYTES = 512 * 1024

internal actual object RemoteLogFileStore {
    private val lock = Any()
    private var file: File? = null

    actual fun init(rootContext: Any?) {
        synchronized(lock) {
            val ctx = rootContext as? Context ?: return
            bindAndroidLogDeviceContext(ctx)
            file = File(ctx.cacheDir, "cloudphoto_remote_logs.ndjson")
        }
    }

    actual fun appendLine(line: String) {
        val f = file ?: return
        synchronized(lock) {
            trimIfNeeded(f)
            f.appendText(line + "\n", Charsets.UTF_8)
        }
    }

    private fun trimIfNeeded(f: File) {
        if (!f.exists() || f.length() <= MAX_FILE_BYTES) return
        val tail = f.readText().takeLast(MAX_FILE_BYTES / 2)
        f.writeText(tail)
    }

    actual fun peekFirstLines(maxLines: Int, maxTotalChars: Int): List<String> {
        val f = file ?: return emptyList()
        synchronized(lock) {
            if (!f.exists() || f.length() == 0L) return emptyList()
            val lines = f.readLines()
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
        }
    }

    actual fun deleteFirstLines(count: Int) {
        val f = file ?: return
        if (count <= 0) return
        synchronized(lock) {
            if (!f.exists()) return
            val lines = f.readLines().filter { it.isNotBlank() }
            if (count >= lines.size) {
                f.delete()
                return
            }
            val remaining = lines.drop(count)
            f.writeText(remaining.joinToString("\n") + "\n")
        }
    }

    actual fun queuedLineCount(): Int {
        val f = file ?: return 0
        synchronized(lock) {
            if (!f.exists()) return 0
            return f.readLines().count { it.isNotBlank() }
        }
    }
}
