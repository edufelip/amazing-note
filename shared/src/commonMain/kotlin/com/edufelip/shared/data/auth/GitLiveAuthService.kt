package com.edufelip.shared.data.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GitLiveAuthService(
    private val auth: FirebaseAuth = Firebase.auth,
) : AuthService {

    override val currentUser: Flow<AuthUser?> =
        auth.authStateChanged.map { user -> user?.toAuthUser() }

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
    }

    override suspend fun setUserName(name: String) {
        auth.currentUser?.updateProfile(name)
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    override suspend fun signInWithGoogle(idToken: String, accessToken: String?) {
        val token = accessToken ?: idToken // gitlive iOS expects non-null accessToken; fallback to idToken
        val credential = GoogleAuthProvider.credential(idToken, token)
        auth.signInWithCredential(credential)
    }

    override suspend fun signInWithApple(idToken: String, rawNonce: String) {
        val credential = OAuthProvider.credential(
            providerId = "apple.com",
            idToken = idToken,
            rawNonce = rawNonce,
        )
        auth.signInWithCredential(credential)
    }

    override suspend fun linkWithApple(idToken: String, rawNonce: String) {
        val credential = OAuthProvider.credential(
            providerId = "apple.com",
            idToken = idToken,
            rawNonce = rawNonce,
        )
        val user = auth.currentUser ?: error("No authenticated user available to link Apple ID.")
        user.linkWithCredential(credential)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteCurrentUser() {
        val user = auth.currentUser ?: error("No authenticated user available to delete.")
        user.delete()
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        val user = auth.currentUser ?: return null
        return user.getIdToken(forceRefresh)
    }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoURL ?: providerData.firstNotNullOfOrNull { it.photoURL },
    )
}
