package com.edufelip.shared.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object NoAuthService : AuthService {
    private val state = MutableStateFlow<AuthUser?>(null)
    override val currentUser: Flow<AuthUser?> = state
    override suspend fun signInWithEmailPassword(email: String, password: String) {
        throw UnsupportedOperationException("Auth not supported on this platform")
    }
    override suspend fun signInWithGoogle(idToken: String) {
        throw UnsupportedOperationException("Auth not supported on this platform")
    }
    override suspend fun signOut() { /* no-op */ }
}

