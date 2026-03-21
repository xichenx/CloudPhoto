package com.xichen.cloudphoto.core.logger

/**
 * 远程上报前脱敏，降低 Token / 密码等误入日志的风险。
 */
internal object LogSanitizer {
    private val patterns = listOf(
        Regex("""(?i)(authorization\s*:\s*)(bearer\s+)[^\s]+""") to "$1$2<redacted>",
        Regex("""(?i)(access[_-]?token["']?\s*[:=]\s*["']?)[^"'\s&]+""") to "$1<redacted>",
        Regex("""(?i)(refresh[_-]?token["']?\s*[:=]\s*["']?)[^"'\s&]+""") to "$1<redacted>",
        Regex("""(?i)(password\s*[:=]\s*["']?)[^"'\s&]+""") to "$1<redacted>",
        Regex("""(?i)(secret[_-]?key\s*[:=]\s*["']?)[^"'\s&]+""") to "$1<redacted>"
    )

    fun sanitize(text: String): String {
        var out = text
        for ((regex, replacement) in patterns) {
            out = regex.replace(out, replacement)
        }
        return out.take(16_000)
    }
}
