@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.ui.util.findTopViewController
import com.edufelip.shared.util.debugLog
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

@Composable
actual fun rememberGoogleSignInLauncher(config: GoogleSignInConfig): GoogleSignInLauncher? = remember {
    IosGoogleSignInLauncher()
}

private class IosGoogleSignInLauncher : GoogleSignInLauncher {
    private var session: ASWebAuthenticationSession? = null

    @Suppress("UnusedPrivateMember") // Kept to hold a strong reference while the session is active.
    private var presentationContext: PresentationContext? = null

    override suspend fun signIn(): GoogleSignInResult {
        debugLog("GoogleSignIn: signIn() invoked, cancelling previous session if any")
        // Cancel any previously running session to avoid stray callbacks.
        session?.cancel()
        clearSession()

        val client = loadGoogleClientConfig()
        if (client == null) {
            debugLog("GoogleSignIn: missing GoogleService-Info.plist client configuration")
            return GoogleSignInResult(null, null, "Missing GoogleService-Info.plist client configuration.")
        } else {
            debugLog("GoogleSignIn: loaded clientId length=${client.clientId.length}")
        }

        val presenter = findTopViewController()
        if (presenter == null) {
            debugLog("GoogleSignIn: no presenter found for Google Sign-In")
            return GoogleSignInResult(null, null, "Unable to find a window to present Google Sign-In.")
        } else {
            debugLog("GoogleSignIn: presenter resolved ${presenter::class}")
        }
        val application = UIApplication.sharedApplication
        val window = presenter.view.window
            ?: application.keyWindow()
            ?: application.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow

        if (window == null) {
            debugLog("GoogleSignIn: failed to resolve a presentation window")
            return GoogleSignInResult(null, null, "Unable to find a window to present Google Sign-In.")
        } else {
            debugLog("GoogleSignIn: presentation window resolved")
        }

        val verifier = generateCodeVerifier()
        val authUrl = buildAuthUrl(
            clientId = client.clientId,
            redirectScheme = client.reversedClientId,
            codeChallenge = verifier, // plain method
        )
        if (authUrl == null) {
            debugLog("GoogleSignIn: failed to build Google auth URL")
            return GoogleSignInResult(null, null, "Failed to build Google auth URL.")
        } else {
            debugLog("GoogleSignIn: built auth URL $authUrl")
        }

        return suspendCancellableCoroutine { cont ->
            val context = PresentationContext(window)
            presentationContext = context

            val authSession = ASWebAuthenticationSession(
                uRL = authUrl,
                callbackURLScheme = client.reversedClientId,
                completionHandler = completion(cont, client, verifier),
            )
            authSession.presentationContextProvider = context
            authSession.prefersEphemeralWebBrowserSession = false
            session = authSession
            debugLog("GoogleSignIn: starting ASWebAuthenticationSession")

            val started = authSession.start()
            if (!started) {
                debugLog("GoogleSignIn: session failed to start")
                clearSession()
                cont.resume(GoogleSignInResult(idToken = null, accessToken = null, errorMessage = "Unable to start Google Sign-In session."))
                return@suspendCancellableCoroutine
            }

            cont.invokeOnCancellation {
                debugLog("GoogleSignIn: coroutine cancelled, cancelling session")
                dispatch_async(dispatch_get_main_queue()) {
                    session?.cancel()
                    clearSession()
                }
            }
        }
    }

    private fun clearSession() {
        session = null
        presentationContext = null
    }
}

private data class GoogleClientConfig(
    val clientId: String,
    val reversedClientId: String,
)

private fun loadGoogleClientConfig(): GoogleClientConfig? {
    val plistPath = NSBundle.mainBundle.pathForResource("GoogleService-Info", "plist") ?: return null
    val dict = NSDictionary.create(contentsOfFile = plistPath) ?: return null
    val clientId = (dict.objectForKey("CLIENT_ID") as? NSString)?.toString()
        ?: dict.objectForKey("CLIENT_ID") as? String
    val reversed = (dict.objectForKey("REVERSED_CLIENT_ID") as? NSString)?.toString()
        ?: dict.objectForKey("REVERSED_CLIENT_ID") as? String
    if (clientId.isNullOrBlank() || reversed.isNullOrBlank()) return null
    return GoogleClientConfig(clientId, reversed)
}

private fun buildAuthUrl(clientId: String, redirectScheme: String, codeChallenge: String): NSURL? {
    val redirectUri = "$redirectScheme:/oauth2redirect"
    val base = "https://accounts.google.com/o/oauth2/v2/auth"
    val scope = "openid email profile"
    val urlString =
        "$base?client_id=${clientId.pEncode()}&redirect_uri=${redirectUri.pEncode()}&response_type=code" +
            "&scope=${scope.pEncode()}&code_challenge=${codeChallenge.pEncode()}&code_challenge_method=plain&prompt=select_account"
    return NSURL(string = urlString)
}

private fun String.pEncode(): String = buildString {
    for (ch in this@pEncode) {
        if (ch.isLetterOrDigit() || ch == '-' || ch == '.' || ch == '_' || ch == '~') {
            append(ch)
        } else {
            append("%")
            append(ch.code.toString(16).uppercase().padStart(2, '0'))
        }
    }
}

private fun completion(
    cont: CancellableContinuation<GoogleSignInResult>,
    client: GoogleClientConfig,
    verifier: String,
): ASWebAuthenticationSessionCompletionHandler {
    var didResume = false

    fun resumeOnce(result: GoogleSignInResult) {
        if (didResume) return // Ignore duplicate callbacks from iOS
        didResume = true
        try {
            if (cont.isActive) {
                debugLog("GoogleSignIn: resuming continuation (idTokenLen=${result.idToken?.length ?: 0}, error=${result.errorMessage})")
                cont.resume(result)
            }
        } catch (t: Throwable) {
            debugLog("GoogleSignIn resume failed: $t")
        }
    }

    return { callbackUrl: NSURL?, error: platform.Foundation.NSError? ->
        debugLog("GoogleSignIn: completion invoked; hasCallbackUrl=${callbackUrl != null}, error=$error")
        when {
            error != null -> {
                val result = GoogleSignInResult(
                    idToken = null,
                    accessToken = null,
                    errorMessage = error.localizedDescription,
                )
                debugLog("GoogleSignIn result: tokenLen=0, error=${result.errorMessage}")
                dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
            }

            callbackUrl != null -> {
                val code = extractAuthCode(callbackUrl)
                if (code.isNullOrBlank()) {
                    val result = GoogleSignInResult(null, null, "Missing authorization code")
                    debugLog("GoogleSignIn result: tokenLen=0, error=${result.errorMessage}")
                    dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
                } else {
                    debugLog("GoogleSignIn: got auth code (len=${code.length}), starting token exchange")
                    exchangeCodeForIdTokenAsync(
                        code = code,
                        redirectScheme = client.reversedClientId,
                        clientId = client.clientId,
                        codeVerifier = verifier,
                    ) { result ->
                        debugLog(
                            "GoogleSignIn result: tokenLen=${result.idToken?.length ?: 0}, " +
                                "error=${result.errorMessage}",
                        )
                        dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
                    }
                }
            }

            else -> {
                val result = GoogleSignInResult(null, null, "Google sign in canceled")
                debugLog("GoogleSignIn result: tokenLen=0, error=${result.errorMessage}")
                dispatch_async(dispatch_get_main_queue()) { resumeOnce(result) }
            }
        }
    }
}

private fun extractAuthCode(callbackUrl: NSURL): String? {
    val components = NSURLComponents(uRL = callbackUrl, resolvingAgainstBaseURL = false)
    components?.queryItems?.forEach { any ->
        val qi = any as? NSURLQueryItem ?: return@forEach
        val name = qi.name
        val value = qi.value
        if (name == "code" && !value.isNullOrBlank()) return value
    }
    return null
}

private fun generateCodeVerifier(): String {
    val raw = NSUUID().UUIDString + NSUUID().UUIDString
    return raw.filter { it.isLetterOrDigit() || it in "-._~" }.take(64)
}

private fun exchangeCodeForIdTokenAsync(
    code: String,
    redirectScheme: String,
    clientId: String,
    codeVerifier: String,
    resume: (GoogleSignInResult) -> Unit,
) {
    // Google native OAuth spec uses "/oauth2redirect" by default; must match the whitelisted URI.
    val redirectUri = "$redirectScheme:/oauth2redirect"
    val tokenUrl = NSURL(string = "https://oauth2.googleapis.com/token")
    if (tokenUrl == null) {
        resume(GoogleSignInResult(null, null, "Invalid token endpoint"))
        return
    }

    val params = mapOf(
        "code" to code,
        "client_id" to clientId,
        "code_verifier" to codeVerifier,
        "redirect_uri" to redirectUri,
        "grant_type" to "authorization_code",
    )
    val body = params.entries.joinToString("&") { "${it.key}=${it.value.pEncode()}" }

    val request = NSMutableURLRequest.requestWithURL(tokenUrl).apply {
        setHTTPMethod("POST")
        setCachePolicy(NSURLRequestReloadIgnoringLocalCacheData)
        // Explicit value/label form keeps the correct Foundation overload in Kotlin/Native.
        setValue(value = "application/x-www-form-urlencoded", forHTTPHeaderField = "Content-Type")
        setHTTPBody((body as NSString).dataUsingEncoding(NSUTF8StringEncoding))
    }
    debugLog("GoogleSignIn: token exchange request prepared (codeLen=${code.length})")

    val task = NSURLSession.sharedSession.dataTaskWithRequest(
        request = request,
        completionHandler = { data, _, error ->
            debugLog("GoogleSignIn: token exchange response received; data=${data != null}, error=$error")
            val result = when {
                error != null -> GoogleSignInResult(null, null, error.localizedDescription ?: "Request failed")
                data == null -> GoogleSignInResult(null, null, "Empty token response")
                else -> {
                    val obj = NSJSONSerialization.JSONObjectWithData(
                        data = data,
                        options = 0uL,
                        error = null,
                    ) as? Map<*, *>
                    val idToken = obj?.get("id_token") as? String
                    val accessToken = obj?.get("access_token") as? String
                    if (idToken.isNullOrBlank()) {
                        GoogleSignInResult(null, accessToken, "Token exchange did not return id_token")
                    } else {
                        GoogleSignInResult(idToken, accessToken, null)
                    }
                }
            }
            resume(result)
        },
    )
    debugLog("GoogleSignIn: token exchange task resumed")
    task.resume()
}

private class PresentationContext(
    private val anchor: ASPresentationAnchor,
) : NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor = anchor
}
