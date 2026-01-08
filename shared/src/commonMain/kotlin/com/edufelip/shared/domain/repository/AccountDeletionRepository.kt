package com.edufelip.shared.domain.repository

sealed interface AccountDeletionResult {
    data object Success : AccountDeletionResult
    data object NotAuthenticated : AccountDeletionResult
    data class Failure(val message: String? = null) : AccountDeletionResult
}

interface AccountDeletionRepository {
    suspend fun deleteAccount(): AccountDeletionResult
}
