@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.security.secureRandomBytes
import com.edufelip.shared.ui.util.findTopViewController
import com.edufelip.shared.util.debugLog
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.CoreCrypto.CC_SHA256
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSPersonNameComponents
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.coroutines.resume

private const val NONCE_LENGTH = 32
private const val SHA256_LENGTH = 32

@Composable
actual fun rememberAppleSignInLauncher(): AppleSignInLauncher? = remember {
    IosAppleSignInLauncher()
}

private class IosAppleSignInLauncher : AppleSignInLauncher {
    private var controller: ASAuthorizationController? = null
    private var delegate: AppleAuthorizationDelegate? = null
    private var presentationContext: ApplePresentationContext? = null

    override suspend fun signIn(): AppleSignInResult {
        controller = null
        delegate = null
        presentationContext = null

        val presenter = findTopViewController()
        if (presenter == null) {
            return AppleSignInResult(
                idToken = null,
                rawNonce = null,
                fullName = null,
                email = null,
                errorMessage = "Unable to present Apple Sign-In.",
            )
        }

        val application = UIApplication.sharedApplication
        val window = presenter.view.window
            ?: application.keyWindow()
            ?: application.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow

        if (window == null) {
            return AppleSignInResult(
                idToken = null,
                rawNonce = null,
                fullName = null,
                email = null,
                errorMessage = "Unable to present Apple Sign-In.",
            )
        }

        val rawNonce = generateNonce()
        val hashedNonce = sha256(rawNonce)

        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = hashedNonce
        }

        return suspendCancellableCoroutine { cont ->
            val authController = ASAuthorizationController(authorizationRequests = listOf(request))
            val context = ApplePresentationContext(window)
            val delegate = AppleAuthorizationDelegate(rawNonce, cont) {
                controller = null
                this.delegate = null
                presentationContext = null
            }

            authController.presentationContextProvider = context
            authController.delegate = delegate

            controller = authController
            this.delegate = delegate
            presentationContext = context

            debugLog("AppleSignIn: starting authorization")
            authController.performRequests()

            cont.invokeOnCancellation {
                controller = null
                this.delegate = null
                presentationContext = null
            }
        }
    }
}

private class AppleAuthorizationDelegate(
    private val rawNonce: String,
    private val continuation: CancellableContinuation<AppleSignInResult>,
    private val onFinish: () -> Unit,
) : NSObject(), ASAuthorizationControllerDelegateProtocol {
    private var didResume = false

    private fun resumeOnce(result: AppleSignInResult) {
        if (didResume) return
        didResume = true
        onFinish()
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val idToken = credential?.identityToken?.toUtf8String()
        val fullName = credential?.fullName.toDisplayName()
        val email = credential?.email

        val result = when {
            idToken.isNullOrBlank() -> AppleSignInResult(
                idToken = null,
                rawNonce = rawNonce,
                fullName = fullName,
                email = email,
                errorMessage = "Missing Apple identity token.",
            )
            else -> AppleSignInResult(
                idToken = idToken,
                rawNonce = rawNonce,
                fullName = fullName,
                email = email,
                errorMessage = null,
            )
        }

        dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        val message = didCompleteWithError.localizedDescription ?: "Apple sign-in failed."
        val result = AppleSignInResult(
            idToken = null,
            rawNonce = rawNonce,
            fullName = null,
            email = null,
            errorMessage = message,
        )
        dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
    }
}

private class ApplePresentationContext(
    private val anchor: ASPresentationAnchor,
) : NSObject(), ASAuthorizationControllerPresentationContextProvidingProtocol {
    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor = anchor
}

private fun generateNonce(): String {
    val bytes = secureRandomBytes(NONCE_LENGTH)
    return bytes.toHexString()
}

private fun sha256(input: String): String {
    val data = input.encodeToByteArray()
    val digest = ByteArray(SHA256_LENGTH)
    data.usePinned { dataPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(
                dataPinned.addressOf(0),
                data.size.convert(),
                digestPinned.addressOf(0).reinterpret<UByteVar>(),
            )
        }
    }
    return digest.toHexString()
}

private fun NSData.toUtf8String(): String? {
    val length = this.length.toInt()
    if (length == 0) return ""
    val source = this.bytes ?: return null
    val buffer = ByteArray(length)
    buffer.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, length.convert())
    }
    return runCatching { buffer.decodeToString() }.getOrNull()
}

private fun ByteArray.toHexString(): String {
    val result = StringBuilder(size * 2)
    for (byte in this) {
        val value = byte.toInt() and 0xff
        result.append(HEX_CHARS[value ushr 4])
        result.append(HEX_CHARS[value and 0x0f])
    }
    return result.toString()
}

private val HEX_CHARS = "0123456789abcdef".toCharArray()

private fun NSPersonNameComponents?.toDisplayName(): String? {
    if (this == null) return null
    val parts = listOfNotNull(givenName, familyName).filter { it.isNotBlank() }
    return parts.joinToString(" ").takeIf { it.isNotBlank() }
}
