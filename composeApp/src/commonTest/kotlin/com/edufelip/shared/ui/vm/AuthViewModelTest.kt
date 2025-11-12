package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthViewModelTest {

    @Test
    fun loginWithEmailUpdatesUserAndClearsError() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("user@test.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@test.com", state.user?.email)
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(listOf("user@test.com" to "secret"), repository.loginRequests)
    }

    @Test
    fun loginWithEmailPropagatesErrorMessage() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository().apply {
            loginError = IllegalStateException("Invalid credentials")
        }
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("user@test.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Invalid credentials", state.error)
        assertFalse(state.loading)
    }

    @Test
    fun signUpEmitsSuccessMessage() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.take(1).collect { events += it } }

        viewModel.signUp("user@test.com", "Secret123!", "Secret123!")
        advanceUntilIdle()
        job.cancel()

        assertTrue(events.contains(AuthEvent.SignUpSuccess))
    }

    @Test
    fun loginValidationFailureDoesNotCallRepository() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("", "short")
        advanceUntilIdle()

        assertTrue(repository.loginRequests.isEmpty())
        assertEquals("Email is required", viewModel.uiState.value.error)
    }

    @Test
    fun passwordResetEmitsResetEmailMessage() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.take(1).collect { events += it } }

        viewModel.sendPasswordReset("user@test.com")
        advanceUntilIdle()
        job.cancel()

        assertTrue(events.firstOrNull() == AuthEvent.PasswordResetSent("user@test.com"))
        assertEquals(listOf("user@test.com"), repository.resetRequests)
    }

    @Test
    fun signUpRejectsMismatchedPasswords() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.signUp("user@test.com", "Secret123!", "Different123!")
        advanceUntilIdle()

        assertTrue(repository.signUpRequests.isEmpty())
        assertEquals("Passwords must match", viewModel.uiState.value.error)
    }

    private fun TestScope.createViewModel(
        repository: FakeAuthRepository,
        dispatcher: CoroutineDispatcher,
    ): AuthViewModel {
        val useCases = buildAuthUseCases(repository)
        return AuthViewModel(useCases, dispatcher)
    }
}

private fun runAuthTest(block: suspend TestScope.(StandardTestDispatcher) -> Unit) = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(dispatcher)
    try {
        block(dispatcher)
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
