package com.xichen.cloudphoto.core.logger

/**
 * 远程上报用日志的本地 NDJSON 持久化（应用缓存目录，不上传备份）。
 */
internal expect object RemoteLogFileStore {
    fun init(rootContext: Any?)

    fun appendLine(line: String)

    /**
     * 查看队首一批日志行（不删除）。
     */
    fun peekFirstLines(maxLines: Int, maxTotalChars: Int): List<String>

    /**
     * 从文件头删除前 [count] 行（上传成功后调用）。
     */
    fun deleteFirstLines(count: Int)

    /** 当前队列行数（近似，用于调试）。 */
    fun queuedLineCount(): Int
}
