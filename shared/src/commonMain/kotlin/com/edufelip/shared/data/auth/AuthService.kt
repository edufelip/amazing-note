package com.edufelip.shared.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthService {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithEmailPassword(email: String, password: String)
    suspend fun signUpWithEmailPassword(email: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signInWithGoogle(idToken: String, accessToken: String?)
    suspend fun signInWithApple(idToken: String, rawNonce: String)
    suspend fun linkWithApple(idToken: String, rawNonce: String)
    suspend fun signOut()
    suspend fun setUserName(name: String)
    suspend fun deleteCurrentUser()
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
}
