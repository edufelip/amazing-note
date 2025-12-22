package com.edufelip.shared.data.repository

import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.auth.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAuthRepositoryTest {

    private lateinit var service: FakeAuthService
    private lateinit var repository: DefaultAuthRepository

    @BeforeTest
    fun setUp() {
        service = FakeAuthService()
        repository = DefaultAuthRepository(service)
    }

    @Test
    fun currentUserDelegatesToService() = runTest {
        val user = AuthUser("uid", "Name", "email", null)
        service.currentUserFlow.value = user
        val result = repository.currentUser.first()
        assertEquals(user, result)
    }

    @Test
    fun signInWithEmailPasswordCallsService() = runTest {
        repository.signInWithEmailPassword("email", "password")
        assertEquals(listOf("email" to "password"), service.signInCalls)
    }

    @Test
    fun signUpWithEmailPasswordCallsService() = runTest {
        repository.signUpWithEmailPassword("email", "password")
        assertEquals(listOf("email" to "password"), service.signUpCalls)
    }

    @Test
    fun signOutCallsService() = runTest {
        repository.signOut()
        assertEquals(1, service.signOutCount)
    }

    private class FakeAuthService : AuthService {
        val currentUserFlow = MutableStateFlow<AuthUser?>(null)
        override val currentUser: Flow<AuthUser?> = currentUserFlow

        val signInCalls = mutableListOf<Pair<String, String>>()
        val signUpCalls = mutableListOf<Pair<String, String>>()
        var signOutCount = 0

        override suspend fun signInWithEmailPassword(email: String, password: String) {
            signInCalls += email to password
        }

        override suspend fun signUpWithEmailPassword(email: String, password: String) {
            signUpCalls += email to password
        }

        override suspend fun sendPasswordResetEmail(email: String) {}

        override suspend fun signInWithGoogle(idToken: String, accessToken: String?) {}

        override suspend fun signOut() {
            signOutCount++
        }

        override suspend fun setUserName(name: String) {}
    }
}
