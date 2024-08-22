package com.emarsys.core.watchdog.connection

import com.emarsys.core.log.Logger
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.SystemConfiguration.SCNetworkReachabilityCallBack
import platform.SystemConfiguration.SCNetworkReachabilityContext
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithAddress
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.SCNetworkReachabilityRef
import platform.SystemConfiguration.SCNetworkReachabilitySetCallback
import platform.SystemConfiguration.SCNetworkReachabilitySetDispatchQueue
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionAutomatic
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnDemand
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionOnTraffic
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsInterventionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsIsDirect
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsIsLocalAddress
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsIsWWAN
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsTransientConnection
import platform.darwin.dispatch_queue_attr_make_with_qos_class
import platform.darwin.dispatch_queue_create
import platform.posix.AF_INET
import platform.posix.QOS_CLASS_DEFAULT
import platform.posix.sockaddr_in

@OptIn(ExperimentalForeignApi::class)
class ReachabilityWrapper(
    private val logger: Logger,
    private val sdkDispatcher: CoroutineDispatcher
) : Reachability {

    private val reachabilityRef: SCNetworkReachabilityRef

    init {
        val zeroAddress = nativeHeap.alloc<sockaddr_in>().apply {
            sin_len = sizeOf<sockaddr_in>().toUByte()
            sin_family = AF_INET.convert()
        }

        reachabilityRef =
            SCNetworkReachabilityCreateWithAddress(null, zeroAddress.ptr.reinterpret())
                ?: throw IllegalStateException("Failed on SCNetworkReachabilityCreateWithAddress")
    }

    override fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit) {
        val dispatchQueueAttr = dispatch_queue_attr_make_with_qos_class(null, QOS_CLASS_DEFAULT, 0)
        val reachabilitySerialQueue = dispatch_queue_create("com.emarsys.sdk", dispatchQueueAttr)

        val callbackContext = CallbackContext(lambda, logger, sdkDispatcher)
        val contextPtr = StableRef.create(callbackContext).asCPointer()

        val context = nativeHeap.alloc<SCNetworkReachabilityContext>().apply {
            version = 0
            info = contextPtr
            retain = null
            release = staticCFunction { it: COpaquePointer? ->
                it?.asStableRef<CallbackContext>()?.dispose()
            }
            copyDescription = null
        }

        val callback: SCNetworkReachabilityCallBack =
            staticCFunction { _: SCNetworkReachabilityRef?, _: SCNetworkReachabilityFlags, info: COpaquePointer? ->
                val context = info?.asStableRef<CallbackContext>()?.get()
                context?.let {
                    try {
                        NSNotificationCenter.defaultCenter.postNotificationName(
                            "ReachabilityChangedNotification",
                            null
                        )
                    } catch (error: Throwable) {
                        CoroutineScope(it.dispatcher).launch {
                            it.logger.error("ConnectionWatchdog", error)
                        }
                    }
                }
            }

        if (!SCNetworkReachabilitySetCallback(reachabilityRef, callback, context.ptr)) {
            throw IllegalStateException("Failed on SCNetworkReachabilitySetCallback")
        }
        if (!SCNetworkReachabilitySetDispatchQueue(reachabilityRef, reachabilitySerialQueue)) {
            throw IllegalStateException("Failed on SCNetworkReachabilitySetDispatchQueue")
        }
    }

    override fun getNetworkConnection(): NetworkConnection {
        return reachabilityRef.getCurrentNetworkConnection()
    }

    override fun isConnected(): Boolean {
        return reachabilityRef.isConnected()
    }

    private fun SCNetworkReachabilityRef.isConnected(): Boolean {
        val flags = getReachabilityFlags()
        val isReachable = flags.contains(kSCNetworkReachabilityFlagsReachable)
        val needsConnection = flags.contains(kSCNetworkReachabilityFlagsConnectionRequired)
        return isReachable && !needsConnection
    }

    private fun SCNetworkReachabilityRef.getCurrentNetworkConnection(): NetworkConnection {
        val flags = getReachabilityFlags()
        val isReachable = flags.contains(kSCNetworkReachabilityFlagsReachable)
        val needsConnection = flags.contains(kSCNetworkReachabilityFlagsConnectionRequired)
        val isMobileConnection = flags.contains(kSCNetworkReachabilityFlagsIsWWAN)

        return when {
            !isReachable || needsConnection -> NetworkConnection.NONE
            isMobileConnection -> NetworkConnection.CELLULAR
            else -> NetworkConnection.WIFI
        }
    }

    private fun fetchReachabilityFlagsFromPlatform(
        reachabilityRef: SCNetworkReachabilityRef,
    ): SCNetworkReachabilityFlags? = memScoped {
        val flags = alloc<SCNetworkReachabilityFlagsVar>()
        return (if (SCNetworkReachabilityGetFlags(
                reachabilityRef,
                flags.ptr
            )
        ) flags.value else null).also {
            CoroutineScope(sdkDispatcher).launch {
                logger.debug("Reachability", " ReachabilityUtil getFlags - $it")
            }
        }
    }

    private fun SCNetworkReachabilityRef.getReachabilityFlags(): Array<SCNetworkReachabilityFlags> {
        val flags = fetchReachabilityFlagsFromPlatform(this) ?: return emptyArray()

        val result = arrayOf(
            kSCNetworkReachabilityFlagsTransientConnection,
            kSCNetworkReachabilityFlagsReachable,
            kSCNetworkReachabilityFlagsConnectionRequired,
            kSCNetworkReachabilityFlagsConnectionOnTraffic,
            kSCNetworkReachabilityFlagsInterventionRequired,
            kSCNetworkReachabilityFlagsConnectionOnDemand,
            kSCNetworkReachabilityFlagsIsLocalAddress,
            kSCNetworkReachabilityFlagsIsDirect,
            kSCNetworkReachabilityFlagsIsWWAN,
            kSCNetworkReachabilityFlagsConnectionAutomatic
        ).filter {
            (flags and it) > 0u
        }
            .toTypedArray()
        CoroutineScope(sdkDispatcher).launch {
            logger.debug(
                "Reachability",
                "SCNetworkReachabilityFlags: ${result.contentDeepToString()}"
            )
        }

        return result
    }

    data class CallbackContext(
        val lambda: (Boolean) -> Unit,
        val logger: Logger,
        val dispatcher: CoroutineDispatcher
    )
}