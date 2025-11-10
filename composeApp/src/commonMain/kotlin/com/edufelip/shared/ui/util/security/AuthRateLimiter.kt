package com.edufelip.shared.ui.util.security

import kotlin.collections.ArrayDeque

class AuthRateLimiter(
    private val windowMillis: Long = 60_000,
    private val maxAttempts: Int = 5,
    private val cooldownMillis: Long = 15_000,
) {
    private val attempts = ArrayDeque<Long>()
    private var lockoutUntil: Long = 0L

    fun canAttempt(now: Long): Boolean = now >= lockoutUntil

    fun registerFailure(now: Long): Long {
        prune(now)
        attempts.addLast(now)
        if (attempts.size >= maxAttempts) {
            lockoutUntil = now + cooldownMillis
            attempts.clear()
        }
        return lockoutUntil
    }

    fun lockoutRemaining(now: Long): Long = (lockoutUntil - now).coerceAtLeast(0L)

    fun lockoutDeadline(): Long = lockoutUntil

    fun reset() {
        attempts.clear()
        lockoutUntil = 0L
    }

    private fun prune(now: Long) {
        while (attempts.isNotEmpty() && now - attempts.first() > windowMillis) {
            attempts.removeFirst()
        }
    }
}
