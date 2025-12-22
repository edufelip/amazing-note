package com.edufelip.shared.domain.usecase

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthUseCasesTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCases: AuthUseCases

    @BeforeTest
    fun setUp() {
        repository = FakeAuthRepository()
        useCases = buildAuthUseCases(repository)
    }

    @Test
    fun observeCurrentUserDelegatesToRepository() = runTest {
        val user = AuthUser("uid-1", "User 1", "user1@example.com", null)
        repository.currentUserFlow.value = user
        val result = useCases.observeCurrentUser().first()
        assertEquals(user, result)
    }

    @Test
    fun loginCallsRepository() = runTest {
        useCases.login("test@example.com", "password")
        assertEquals(listOf("test@example.com" to "password"), repository.loginCalls)
    }

    @Test
    fun signUpCallsRepository() = runTest {
        useCases.signUp("Name", "test@example.com", "password")
        assertEquals(listOf("test@example.com" to "password"), repository.signUpCalls)
    }

    @Test
    fun validateEmailReturnsValidForCorrectEmail() {
        val result = useCases.validateEmail("test@example.com")
        assertTrue(result.isValid)
    }

    @Test
    fun validateEmailReturnsInvalidForEmptyEmail() {
        val result = useCases.validateEmail("")
        assertTrue(!result.isValid)
    }

    @Test
    fun validatePasswordReturnsValidForCorrectPassword() {
        val result = useCases.validatePassword("Password123!")
        assertTrue(result.isValid)
    }

    private class FakeAuthRepository : AuthRepository {
        val currentUserFlow = MutableStateFlow<AuthUser?>(null)
        override val currentUser: Flow<AuthUser?> = currentUserFlow

        val loginCalls = mutableListOf<Pair<String, String>>()
        val signUpCalls = mutableListOf<Pair<String, String>>()
        val resetPasswordCalls = mutableListOf<String>()
        val googleSignInCalls = mutableListOf<Pair<String, String?>>()
        val updateNameCalls = mutableListOf<String>()
        var signOutCount = 0

        override suspend fun signInWithEmailPassword(email: String, password: String) {
            loginCalls += email to password
        }

        override suspend fun signUpWithEmailPassword(email: String, password: String) {
            signUpCalls += email to password
        }

        // Fix signUp call signature to match interface
        override suspend fun setUserName(name: String) {
            updateNameCalls += name
        }

        override suspend fun sendPasswordResetEmail(email: String) {
            resetPasswordCalls += email
        }

        override suspend fun signInWithGoogle(idToken: String, accessToken: String?) {
            googleSignInCalls += idToken to accessToken
        }

        override suspend fun signOut() {
            signOutCount++
        }
    }
}
