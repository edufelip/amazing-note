package com.edufelip.shared.ui.util.security

import com.edufelip.shared.ui.util.platform.platformBehavior

object SecurityLogger {

    fun logValidationFailure(flow: String, field: String, reason: String, rawSample: String) {
        println(
            "[Security][flow=$flow][field=$field][reason=$reason][platform=${platform()}][sample=${hash(rawSample)}]",
        )
    }

    fun logRateLimit(flow: String, cooldownMillis: Long) {
        println(
            "[Security][flow=$flow][event=rate_limit][cooldown_ms=$cooldownMillis][platform=${platform()}]",
        )
    }

    fun logSanitized(flow: String, field: String, rawSample: String) {
        println(
            "[Security][flow=$flow][field=$field][event=sanitized][platform=${platform()}][sample=${hash(rawSample)}]",
        )
    }

    private fun platform(): String = platformBehavior().platformName

    private fun hash(value: String): String = value.trim().lowercase().hashCode().toString(16)
}
