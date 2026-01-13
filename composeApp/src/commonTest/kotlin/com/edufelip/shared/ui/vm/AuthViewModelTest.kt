package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AccountDeletionRepository
import com.edufelip.shared.domain.repository.AccountDeletionResult
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Test
    fun loginWithEmailUpdatesUserAndClearsError() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("user@test.com", "Password123!")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user@test.com", state.user?.email)
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(listOf("user@test.com" to "Password123!"), repository.loginRequests)
    }

    @Test
    fun loginWithEmailInvalidCredentialsShowsSpecificError() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository().apply {
            loginError = IllegalStateException("The supplied auth credential is incorrect")
        }
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("user@test.com", "Password123!")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthError.InvalidCredentials, state.error)
        assertFalse(state.loading)
    }

    @Test
    fun loginWithEmailNetworkFailureEmitsNetworkError() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository().apply {
            loginError = IllegalStateException("Network request failed")
        }
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.loginWithEmail("user@test.com", "Password123!")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthError.Network, state.error)
        assertFalse(state.loading)
    }

    @Test
    fun signUpEmitsSuccessMessage() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.collect { events += it } }

        viewModel.signUp("Name", "user@test.com", "Password123!", "Password123!")
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
        assertTrue(viewModel.uiState.value.error is AuthError.Custom)
    }

    @Test
    fun passwordResetEmitsResetEmailMessage() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.collect { events += it } }

        viewModel.sendPasswordReset("user@test.com")
        advanceUntilIdle()
        job.cancel()

        assertTrue(events.any { it is AuthEvent.PasswordResetSent && it.email == "user@test.com" })
        assertEquals(listOf("user@test.com"), repository.resetRequests)
    }

    @Test
    fun signUpRejectsMismatchedPasswords() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.signUp("Name", "user@test.com", "Password123!", "Different123!")
        advanceUntilIdle()

        assertTrue(repository.signUpRequests.isEmpty())
        assertEquals(AuthError.Custom("Passwords must match"), viewModel.uiState.value.error)
    }

    @Test
    fun signInWithAppleEmitsLoginSuccess() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository, dispatcher)

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.collect { events += it } }

        viewModel.signInWithAppleToken("token", "nonce", "Apple User", "apple@example.com")
        advanceUntilIdle()
        job.cancel()

        assertTrue(events.contains(AuthEvent.LoginSuccess))
        assertEquals(listOf("token" to "nonce"), repository.appleSignInRequests)
    }

    @Test
    fun signInWithAppleAccountCollisionStoresPendingLink() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository().apply {
            appleSignInError = IllegalStateException("account-exists-with-different-credential")
        }
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.signInWithAppleToken("token", "nonce", "Apple User", "apple@example.com")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error is AuthError.Custom)
        assertEquals("token", state.pendingAppleLink?.idToken)
        assertEquals("nonce", state.pendingAppleLink?.rawNonce)
    }

    @Test
    fun loginLinksPendingAppleCredential() = runAuthTest { dispatcher ->
        val repository = FakeAuthRepository().apply {
            appleSignInError = IllegalStateException("account-exists-with-different-credential")
        }
        val viewModel = createViewModel(repository, dispatcher)

        viewModel.signInWithAppleToken("token", "nonce", "Apple User", "apple@example.com")
        advanceUntilIdle()

        viewModel.loginWithEmail("user@test.com", "Password123!")
        advanceUntilIdle()

        assertEquals(listOf("token" to "nonce"), repository.appleLinkRequests)
    }

    private fun TestScope.createViewModel(
        repository: FakeAuthRepository,
        dispatcher: CoroutineDispatcher,
    ): AuthViewModel {
        val useCases = buildAuthUseCases(repository, FakeAccountDeletionRepository())
        return AuthViewModel(useCases, dispatcher)
    }
}

private fun runAuthTest(block: suspend TestScope.(CoroutineDispatcher) -> Unit) = runTest {
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
    var appleSignInError: Throwable? = null
    var appleLinkError: Throwable? = null

    val loginRequests = mutableListOf<Pair<String, String>>()
    val signUpRequests = mutableListOf<Pair<String, String>>()
    val resetRequests = mutableListOf<String>()
    val appleSignInRequests = mutableListOf<Pair<String, String>>()
    val appleLinkRequests = mutableListOf<Pair<String, String>>()

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

    override suspend fun setUserName(name: String) {
        val newUser = _currentUser.value?.copy(displayName = name)
        _currentUser.value = newUser
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        signUpRequests += email to password
        signUpError?.let { throw it }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        resetRequests += email
    }

    override suspend fun signInWithGoogle(idToken: String, accessToken: String?) {
        _currentUser.value = AuthUser(
            uid = "google",
            displayName = "Google User",
            email = null,
            photoUrl = null,
        )
    }

    override suspend fun signInWithApple(idToken: String, rawNonce: String) {
        appleSignInRequests += idToken to rawNonce
        appleSignInError?.let { throw it }
        _currentUser.value = AuthUser(
            uid = "apple",
            displayName = "Apple User",
            email = null,
            photoUrl = null,
        )
    }

    override suspend fun linkWithApple(idToken: String, rawNonce: String) {
        appleLinkRequests += idToken to rawNonce
        appleLinkError?.let { throw it }
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}

private class FakeAccountDeletionRepository : AccountDeletionRepository {
    override suspend fun deleteAccount(): AccountDeletionResult = AccountDeletionResult.Success
}
