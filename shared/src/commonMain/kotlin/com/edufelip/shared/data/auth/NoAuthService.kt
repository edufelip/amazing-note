package com.edufelip.shared.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object NoAuthService : AuthService {
    private val state = MutableStateFlow<AuthUser?>(null)
    override val currentUser: Flow<AuthUser?> = state
    override suspend fun signInWithEmailPassword(email: String, password: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun signUpWithEmailPassword(email: String, password: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun setUserName(name: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun sendPasswordResetEmail(email: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun signInWithGoogle(idToken: String, accessToken: String?): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun signInWithApple(idToken: String, rawNonce: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun linkWithApple(idToken: String, rawNonce: String): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun signOut() { /* no-op */ }
    override suspend fun deleteCurrentUser(): Unit = throw UnsupportedOperationException("Auth not supported on this platform")
    override suspend fun getIdToken(forceRefresh: Boolean): String? = null
}
