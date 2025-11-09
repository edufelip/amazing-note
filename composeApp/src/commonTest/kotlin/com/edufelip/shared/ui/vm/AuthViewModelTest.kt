package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthViewModelTest {

    @Test
    fun loginWithEmailUpdatesUserAndClearsError() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.loginWithEmail("user@test.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@test.com", state.user?.email)
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(listOf("user@test.com" to "secret"), repository.loginRequests)
    }

    @Test
    fun loginWithEmailPropagatesErrorMessage() = runAuthTest {
        val repository = FakeAuthRepository().apply {
            loginError = IllegalStateException("Invalid credentials")
        }
        val viewModel = createViewModel(repository)

        viewModel.loginWithEmail("user@test.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Invalid credentials", state.error)
        assertFalse(state.loading)
    }

    @Test
    fun signUpEmitsSuccessMessage() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.signUp("user@test.com", "secret")
        advanceUntilIdle()

        assertIs<AuthMessage.SignUpSuccess>(viewModel.uiState.value.message)
    }

    @Test
    fun passwordResetEmitsResetEmailMessage() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.sendPasswordReset("user@test.com")
        advanceUntilIdle()

        assertIs<AuthMessage.ResetEmailSent>(viewModel.uiState.value.message)
        assertEquals(listOf("user@test.com"), repository.resetRequests)
    }

    private fun TestScope.createViewModel(repository: FakeAuthRepository): AuthViewModel {
        val useCases = buildAuthUseCases(repository)
        return AuthViewModel(useCases, this)
    }
}

private fun runAuthTest(block: suspend TestScope.() -> Unit) = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(dispatcher)
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}

private class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)

    var loginError: Throwable? = null
    var signUpError: Throwable? = null

    val loginRequests = mutableListOf<Pair<String, String>>()
    val signUpRequests = mutableListOf<Pair<String, String>>()
    val resetRequests = mutableListOf<String>()

    override val currentUser: Flow<AuthUser?> = _currentUser

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        loginRequests += email to password
        loginError?.let { throw it }
        _currentUser.value = AuthUser(
            uid = "uid-$email",
            displayName = "User",
            email = email,
            photoUrl = null,
        )
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        signUpRequests += email to password
        signUpError?.let { throw it }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        resetRequests += email
    }

    override suspend fun signInWithGoogle(idToken: String) {
        _currentUser.value = AuthUser(
            uid = "google",
            displayName = "Google User",
            email = null,
            photoUrl = null,
        )
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}
