package com.edufelip.shared.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

class IosFirebaseAuthService : AuthService {
    private fun mapUser(u: FIRUser?): AuthUser? = u?.let {
        AuthUser(
            uid = it.uid,
            displayName = it.displayName,
            email = it.email,
            photoUrl = it.photoURL?.absoluteString
        )
    }

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val auth = FIRAuth.auth()
        val handle = auth?.addAuthStateDidChangeListener { _, user ->
            trySend(mapUser(user))
        }
        // Send current state immediately
        trySend(mapUser(auth?.currentUser))
        awaitClose {
            if (handle != null) {
                auth?.removeAuthStateDidChangeListener(handle)
            }
        }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        val auth = FIRAuth.auth() ?: error("FIRAuth not available")
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmail(email = email, password = password) { _, error: NSError? ->
                if (error != null) cont.resumeWith(Result.failure(Exception(error.localizedDescription)))
                else cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        val auth = FIRAuth.auth() ?: error("FIRAuth not available")
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmail(email = email, password = password) { _, error: NSError? ->
                if (error != null) cont.resumeWith(Result.failure(Exception(error.localizedDescription)))
                else cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        val auth = FIRAuth.auth() ?: error("FIRAuth not available")
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetWithEmail(email = email) { error: NSError? ->
                if (error != null) cont.resumeWith(Result.failure(Exception(error.localizedDescription)))
                else cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        // Not implemented on iOS in this pass (requires GoogleSignIn flow to obtain tokens).
        throw UnsupportedOperationException("Google Sign-In not configured on iOS")
    }

    override suspend fun signOut() {
        val auth = FIRAuth.auth() ?: return
        memScoped {
            val errRef = alloc<ObjCObjectVar<NSError?>>()
            val ok = auth.signOut(errRef.ptr)
            if (!ok) {
                throw Exception(errRef.value?.localizedDescription ?: "Sign out failed")
            }
        }
    }
}

