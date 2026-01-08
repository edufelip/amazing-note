package com.edufelip.shared.ui.vm

import com.edufelip.amazing_note.MainCoroutineRule
import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AccountDeletionRepository
import com.edufelip.shared.domain.repository.AccountDeletionResult
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelLogoutDecisionTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authRepository = FakeAuthRepository()
    private val accountDeletionRepository = FakeAccountDeletionRepository()

    private fun buildViewModel(): AuthViewModel {
        val useCases = buildAuthUseCases(authRepository, accountDeletionRepository)
        return AuthViewModel(useCases, dispatcher = mainCoroutineRule.dispatcher)
    }

    @Test
    fun requestLogoutDecision_allowsLogout_whenUserMissing() = runTest {
        val viewModel = buildViewModel()
        val isOnline = MutableStateFlow(false)
        viewModel.bindLogoutGuard(
            LogoutGuard(
                isOnline = isOnline,
                hasPendingLocalChanges = { true },
            ),
        )

        advanceUntilIdle()

        val decision = viewModel.requestLogoutDecision()
        assertThat(decision).isEqualTo(LogoutDecision.Allowed)
    }

    @Test
    fun requestLogoutDecision_allowsLogout_whenNoGuardBound() = runTest {
        val viewModel = buildViewModel()
        authRepository.currentUserState.value = AuthUser("uid", "Ana", "ana@email.com", null)

        advanceUntilIdle()

        val decision = viewModel.requestLogoutDecision()
        assertThat(decision).isEqualTo(LogoutDecision.Allowed)
    }

    @Test
    fun requestLogoutDecision_blocksLogout_whenOffline() = runTest {
        val viewModel = buildViewModel()
        authRepository.currentUserState.value = AuthUser("uid", "Ana", "ana@email.com", null)
        val isOnline = MutableStateFlow(false)
        viewModel.bindLogoutGuard(
            LogoutGuard(
                isOnline = isOnline,
                hasPendingLocalChanges = { false },
            ),
        )

        advanceUntilIdle()

        val decision = viewModel.requestLogoutDecision()
        assertThat(decision).isEqualTo(LogoutDecision.Offline)
    }

    @Test
    fun requestLogoutDecision_blocksLogout_whenPendingChanges() = runTest {
        val viewModel = buildViewModel()
        authRepository.currentUserState.value = AuthUser("uid", "Ana", "ana@email.com", null)
        val isOnline = MutableStateFlow(true)
        viewModel.bindLogoutGuard(
            LogoutGuard(
                isOnline = isOnline,
                hasPendingLocalChanges = { true },
            ),
        )

        advanceUntilIdle()

        val decision = viewModel.requestLogoutDecision()
        assertThat(decision).isEqualTo(LogoutDecision.PendingChanges)
    }

    @Test
    fun requestLogoutDecision_allowsLogout_whenOnlineAndSynced() = runTest {
        val viewModel = buildViewModel()
        authRepository.currentUserState.value = AuthUser("uid", "Ana", "ana@email.com", null)
        val isOnline = MutableStateFlow(true)
        viewModel.bindLogoutGuard(
            LogoutGuard(
                isOnline = isOnline,
                hasPendingLocalChanges = { false },
            ),
        )

        advanceUntilIdle()

        val decision = viewModel.requestLogoutDecision()
        assertThat(decision).isEqualTo(LogoutDecision.Allowed)
    }

    private class FakeAuthRepository : AuthRepository {
        val currentUserState = MutableStateFlow<AuthUser?>(null)

        override val currentUser: Flow<AuthUser?> = currentUserState

        override suspend fun signInWithEmailPassword(email: String, password: String) = Unit

        override suspend fun signUpWithEmailPassword(email: String, password: String) = Unit

        override suspend fun setUserName(name: String) = Unit

        override suspend fun sendPasswordResetEmail(email: String) = Unit

        override suspend fun signInWithGoogle(idToken: String, accessToken: String?) = Unit

        override suspend fun signInWithApple(idToken: String, rawNonce: String) = Unit

        override suspend fun linkWithApple(idToken: String, rawNonce: String) = Unit

        override suspend fun signOut() = Unit
    }

    private class FakeAccountDeletionRepository : AccountDeletionRepository {
        override suspend fun deleteAccount(): AccountDeletionResult =
            AccountDeletionResult.NotAuthenticated
    }
}
