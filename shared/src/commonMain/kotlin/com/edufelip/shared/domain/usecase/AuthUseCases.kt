package com.edufelip.shared.domain.usecase

import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUser(private val repository: AuthRepository) {
    operator fun invoke(): Flow<AuthUser?> = repository.currentUser
}

class Login(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.signInWithEmailPassword(email, password)
}

class SignUp(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.signUpWithEmailPassword(email, password)
}

class SendPasswordReset(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetEmail(email)
}

class Logout(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

data class AuthUseCases(
    val observeCurrentUser: ObserveCurrentUser,
    val login: Login,
    val signUp: SignUp,
    val sendPasswordReset: SendPasswordReset,
    val logout: Logout,
)

fun buildAuthUseCases(
    repository: AuthRepository,
): AuthUseCases = AuthUseCases(
    observeCurrentUser = ObserveCurrentUser(repository),
    login = Login(repository),
    signUp = SignUp(repository),
    sendPasswordReset = SendPasswordReset(repository),
    logout = Logout(repository),
)
