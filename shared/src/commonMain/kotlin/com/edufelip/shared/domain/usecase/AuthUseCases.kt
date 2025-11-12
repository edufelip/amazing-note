package com.edufelip.shared.domain.usecase

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.validation.CredentialValidationResult
import com.edufelip.shared.domain.validation.EmailValidationResult
import com.edufelip.shared.domain.validation.PasswordConfirmationResult
import com.edufelip.shared.domain.validation.PasswordValidationResult
import com.edufelip.shared.domain.validation.validateEmail
import com.edufelip.shared.domain.validation.validatePassword
import com.edufelip.shared.domain.validation.validatePasswordConfirmation
import kotlinx.coroutines.flow.Flow

class ValidateEmail {
    operator fun invoke(input: String): EmailValidationResult = validateEmail(input)
}

class ValidatePassword {
    operator fun invoke(input: String): PasswordValidationResult = validatePassword(input)
}

class ValidateCredentials(
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
) {
    operator fun invoke(email: String, password: String): CredentialValidationResult {
        val emailResult = validateEmail(email)
        val passwordResult = validatePassword(password)
        return CredentialValidationResult(emailResult, passwordResult)
    }
}

class ValidatePasswordConfirmation {
    operator fun invoke(password: String, confirmation: String): PasswordConfirmationResult = validatePasswordConfirmation(password, confirmation)
}

class ObserveCurrentUser(private val repository: AuthRepository) {
    operator fun invoke(): Flow<AuthUser?> = repository.currentUser
}

class Login(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.signInWithEmailPassword(email, password)
}

class SignUp(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String) = repository.signUpWithEmailPassword(email, password)
}

class SendPasswordReset(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetEmail(email)
}

class Logout(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

class SignInWithGoogle(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String) = repository.signInWithGoogle(token)
}

class UpdateUserName(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String) = repository.setUserName(name)
}

data class AuthUseCases(
    val observeCurrentUser: ObserveCurrentUser,
    val login: Login,
    val signUp: SignUp,
    val sendPasswordReset: SendPasswordReset,
    val logout: Logout,
    val signInWithGoogle: SignInWithGoogle,
    val validateEmail: ValidateEmail,
    val validatePassword: ValidatePassword,
    val validateCredentials: ValidateCredentials,
    val validatePasswordConfirmation: ValidatePasswordConfirmation,
    val updateUserName: UpdateUserName
)

fun buildAuthUseCases(
    repository: AuthRepository,
): AuthUseCases {
    val validateEmail = ValidateEmail()
    val validatePassword = ValidatePassword()
    return AuthUseCases(
        observeCurrentUser = ObserveCurrentUser(repository),
        login = Login(repository),
        signUp = SignUp(repository),
        sendPasswordReset = SendPasswordReset(repository),
        logout = Logout(repository),
        signInWithGoogle = SignInWithGoogle(repository),
        validateEmail = validateEmail,
        validatePassword = validatePassword,
        validateCredentials = ValidateCredentials(validateEmail, validatePassword),
        validatePasswordConfirmation = ValidatePasswordConfirmation(),
        updateUserName = UpdateUserName(repository)
    )
}
