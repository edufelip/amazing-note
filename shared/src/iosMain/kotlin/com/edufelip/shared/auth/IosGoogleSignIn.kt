package com.edufelip.shared.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRGoogleAuthProvider
import cocoapods.GoogleSignIn.GIDSignIn
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

private fun topViewController(root: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController): UIViewController? {
    var current = root
    while (true) {
        val presented = current?.presentedViewController
        if (presented != null) current = presented else break
    }
    return current
}

object IosGoogleSignIn {
    fun requestSignIn(onComplete: (Boolean, String?) -> Unit) {
        val presenter = topViewController()
        if (presenter == null) {
            onComplete(false, "No presenter available")
            return
        }
        GIDSignIn.sharedInstance.signInWithPresentingViewController(presenter) { result, error: NSError? ->
            if (error != null) {
                onComplete(false, error.localizedDescription)
                return@signInWithPresentingViewController
            }
            val user = result?.user
            val idToken = user?.idToken?.tokenString
            val accessToken = user?.accessToken?.tokenString
            if (idToken == null || accessToken == null) {
                onComplete(false, "Missing Google tokens")
                return@signInWithPresentingViewController
            }
            val credential = FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken)
            val auth = FIRAuth.auth()
            auth?.signInWithCredential(credential) { _, err: NSError? ->
                if (err != null) onComplete(false, err.localizedDescription)
                else onComplete(true, null)
            }
        }
    }
}

