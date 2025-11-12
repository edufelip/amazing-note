package com.edufelip.shared.domain.repository

import com.edufelip.shared.data.auth.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithEmailPassword(email: String, password: String)
    suspend fun signUpWithEmailPassword(email: String, password: String)
    suspend fun setUserName(name: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signOut()
}
