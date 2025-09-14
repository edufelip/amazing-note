package com.edufelip.shared.data

import com.edufelip.shared.auth.AuthService
import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    private val service: AuthService,
) : AuthRepository {
    override val currentUser: Flow<AuthUser?> = service.currentUser

    override suspend fun signInWithEmailPassword(email: String, password: String) =
        service.signInWithEmailPassword(email, password)

    override suspend fun signUpWithEmailPassword(email: String, password: String) =
        service.signUpWithEmailPassword(email, password)

    override suspend fun sendPasswordResetEmail(email: String) =
        service.sendPasswordResetEmail(email)

    override suspend fun signInWithGoogle(idToken: String) =
        service.signInWithGoogle(idToken)

    override suspend fun signOut() = service.signOut()
}
