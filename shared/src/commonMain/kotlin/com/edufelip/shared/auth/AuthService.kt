package com.edufelip.shared.auth

import kotlinx.coroutines.flow.Flow

interface AuthService {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithEmailPassword(email: String, password: String)
    suspend fun signUpWithEmailPassword(email: String, password: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signOut()
}
