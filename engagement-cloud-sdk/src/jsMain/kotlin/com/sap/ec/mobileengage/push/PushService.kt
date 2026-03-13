package com.sap.ec.mobileengage.push

import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.device.notification.PermissionState
import com.sap.ec.core.log.Logger
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.push.serviceworker.ServiceWorkerManagerApi
import com.sap.ec.util.runCatchingWithoutCancellation
import js.buffer.BufferSource
import js.typedarrays.toUint8Array
import kotlinx.browser.window
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import web.push.PushSubscription
import web.push.PushSubscriptionOptionsInit
import web.push.getSubscription
import web.push.permissionState
import web.push.subscribe
import web.push.unsubscribe
import web.serviceworker.ServiceWorkerRegistration

internal class PushService(
    private val serviceWorkerManager: ServiceWorkerManagerApi,
    private val webPermissionHandler: PermissionHandlerApi,
    private val storage: StringStorageApi,
    private val sdkLogger: Logger
) : PushServiceApi {

    private var pushSubscription: PushSubscription? = null

    override suspend fun subscribe(): Result<String?> {
        checkAPIBrowserAvailability().onFailure {
            return Result.failure(it)
        }
        if (getPushSubscription() != null) {
            sdkLogger.debug("Already subscribed to push notifications.")
            return Result.success(pushSubscription?.let { JSON.stringify(it) })
        }
        return try {
            val serviceWorkerOptions =
                serviceWorkerManager.getServiceWorkerOptions() ?: return Result.failure(
                    IllegalStateException("Service worker options are not set.")
                )
            println("Registering service worker with options: ${JSON.stringify(serviceWorkerOptions)}")
            serviceWorkerManager.register().onFailure {
                return Result.failure(
                    IllegalStateException(
                        "Service worker registration failed: ${it.message}",
                        it
                    )
                )
            }
            webPermissionHandler.requestPushPermission()
            if (
                getPermissionState() != PermissionState.Granted
            ) {
                return Result.failure(IllegalStateException("Notification permission was not granted."))
            }
            val options = createPushSubscriptionOptions(serviceWorkerOptions)
            println("Subscribing to push notifications with options: ${JSON.stringify(options)}")
            pushSubscription = getServiceWorkerRegistration()?.pushManager?.subscribe(options)
            println("Subscribing to push notifications with options: ${JSON.stringify(options)}")
            val pushToken = pushSubscription?.let {
                val pushToken = JSON.stringify(it)
                storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, pushToken)
                pushToken
            }
            Result.success(pushToken)
        } catch (e: Throwable) {
            println("Error during push subscription: ${e.message}")
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Push subscription failed", e)
            Result.failure<String>(e)
        }
    }

    private suspend fun checkAPIBrowserAvailability(): Result<Unit> {
        if (js("!('serviceWorker' in navigator)")) {
            sdkLogger.debug("Service workers are not supported in this browser. Push notifications will not work.")
            return Result.failure(UnsupportedOperationException("Service workers are not supported in this browser."))  // todo custom exception
        }
        if (js("!('PushManager' in window)")) {
            sdkLogger.debug("Push API is not supported in this browser. Push notifications will not work.")
            return Result.failure(UnsupportedOperationException("Push API is not supported in this browser.")) // todo custom exception
        }
        return Result.success(Unit)
    }

    override suspend fun getServiceWorkerRegistration(): ServiceWorkerRegistration? =
        serviceWorkerManager.getServiceWorkerRegistration()


    override suspend fun getPermissionState(): PermissionState {
        return try {
            serviceWorkerManager.getServiceWorkerOptions()?.let { serviceWorkerOptions ->
                val pushSubscriptionOptions = createPushSubscriptionOptions(serviceWorkerOptions)
                getServiceWorkerRegistration()?.pushManager?.permissionState(pushSubscriptionOptions)
                    ?.let { permissionState ->
                        enumValueOf<PermissionState>(
                            permissionState.toString()
                                .replaceFirstChar { it.titlecase() })
                    }
            } ?: PermissionState.Denied
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Failed to get push permission state", e)
            PermissionState.Denied
        }
    }

    override suspend fun unsubscribe(): Result<Unit> = runCatchingWithoutCancellation {
        getPushSubscription()?.unsubscribe()
        pushSubscription = null
        serviceWorkerManager.unregister()
    }


    private suspend fun getPushSubscription(): PushSubscription? {
        return pushSubscription ?: getServiceWorkerRegistration()?.pushManager?.getSubscription()
            ?.also { pushSubscription = it }
    }

    private fun createPushSubscriptionOptions(serviceWorkerOptions: ServiceWorkerOptions): PushSubscriptionOptionsInit {
        val options = PushSubscriptionOptionsInit(
            applicationServerKey = urlBase64ToUint8Array(serviceWorkerOptions.applicationServerKey),
            userVisibleOnly = true
        )
        return options
    }

    private fun urlBase64ToUint8Array(base64String: String): BufferSource {
        val padding = "=".repeat((4 - base64String.length % 4) % 4)
        val base64 = (base64String + padding)
            .replace('-', '+')
            .replace('_', '/')

        val rawData = window.atob(base64)
        val outputArray = ByteArray(rawData.length)

        for (i in rawData.indices) {
            outputArray[i] = rawData[i].code.toByte()
        }
        return outputArray.toUint8Array()
    }
}
