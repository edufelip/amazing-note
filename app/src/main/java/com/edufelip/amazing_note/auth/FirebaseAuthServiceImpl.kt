package com.edufelip.amazing_note.auth

import com.edufelip.shared.auth.AuthService
import com.edufelip.shared.auth.AuthUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthServiceImpl : AuthService {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(
                auth.currentUser?.let { u ->
                    AuthUser(
                        uid = u.uid,
                        displayName = u.displayName,
                        email = u.email,
                        photoUrl = u.photoUrl?.toString(),
                    )
                },
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(
            firebaseAuth.currentUser?.let { u ->
                AuthUser(
                    uid = u.uid,
                    displayName = u.displayName,
                    email = u.email,
                    photoUrl = u.photoUrl?.toString(),
                )
            },
        )
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        val auth = firebaseAuth
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String) {
        val auth = firebaseAuth
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        val auth = firebaseAuth
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}
