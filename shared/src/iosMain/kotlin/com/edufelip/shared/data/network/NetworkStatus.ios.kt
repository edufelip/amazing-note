@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.data.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreFoundation.CFRunLoopGetMain
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.SystemConfiguration.SCNetworkReachabilityContext
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.SCNetworkReachabilityRef
import platform.SystemConfiguration.SCNetworkReachabilityScheduleWithRunLoop
import platform.SystemConfiguration.SCNetworkReachabilitySetCallback
import platform.SystemConfiguration.SCNetworkReachabilityUnscheduleFromRunLoop
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnDemand
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnTraffic
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsInterventionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

private class IosNetworkStatus : NetworkStatus {
    private val reachability = SCNetworkReachabilityCreateWithName(null, "apple.com")
    private val stableRef = StableRef.create(this)
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        reachability?.let { ref ->
            val context = cValue<SCNetworkReachabilityContext> {
                version = 0
                info = stableRef.asCPointer()
                retain = null
                release = null
                copyDescription = null
            }
            val callback =
                staticCFunction<SCNetworkReachabilityRef?, SCNetworkReachabilityFlags, COpaquePointer?, Unit> {
                    _, flags, info ->
                    info?.asStableRef<IosNetworkStatus>()?.get()?.update(flags)
                }
            SCNetworkReachabilitySetCallback(ref, callback, context)
            SCNetworkReachabilityScheduleWithRunLoop(ref, CFRunLoopGetMain(), kCFRunLoopDefaultMode)
            updateFlags(ref)
        }
    }

    fun dispose() {
        reachability?.let { ref ->
            SCNetworkReachabilitySetCallback(ref, null, null)
            SCNetworkReachabilityUnscheduleFromRunLoop(ref, CFRunLoopGetMain(), kCFRunLoopDefaultMode)
        }
        stableRef.dispose()
    }

    private fun update(flags: SCNetworkReachabilityFlags) {
        _isOnline.value = flags.isReachable()
    }

    private fun updateFlags(ref: SCNetworkReachabilityRef) {
        memScoped {
            val flags = alloc<UIntVar>()
            if (SCNetworkReachabilityGetFlags(ref, flags.ptr)) {
                update(flags.value)
            }
        }
    }
}

@Composable
actual fun rememberNetworkStatus(): NetworkStatus {
    val status = remember { IosNetworkStatus() }
    DisposableEffect(status) {
        onDispose { status.dispose() }
    }
    return status
}

private fun SCNetworkReachabilityFlags.isReachable(): Boolean {
    val raw = toInt()
    val reachable = raw and kSCNetworkReachabilityFlagsReachable.toInt() != 0
    val connectionRequired = raw and kSCNetworkReachabilityFlagsConnectionRequired.toInt() != 0
    val canConnectAutomatically = raw and kSCNetworkReachabilityFlagsConnectionOnDemand.toInt() != 0 ||
        raw and kSCNetworkReachabilityFlagsConnectionOnTraffic.toInt() != 0
    val interventionRequired = raw and kSCNetworkReachabilityFlagsInterventionRequired.toInt() != 0
    return reachable && (!connectionRequired || (canConnectAutomatically && !interventionRequired))
}
