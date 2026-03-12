package com.sap.ec.mobileengage.push

import JsEngagementCloudSDKConfig
import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.api.push.PushConstants
import com.sap.ec.context.SdkContextApi
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
import web.serviceworker.unregister
import web.serviceworker.update

internal class PushService(
    private val serviceWorkerManager: ServiceWorkerManagerApi,
    private val sdkContext: SdkContextApi,
    private val webPermissionHandler: PermissionHandlerApi,
    private val storage: StringStorageApi,
    private val sdkLogger: Logger
) : PushServiceApi {

    private var serviceWorkerRegistration: ServiceWorkerRegistration? = null
    private var pushSubscription: PushSubscription? = null

    override suspend fun subscribe(): Result<String?> {
        if (js("!('serviceWorker' in navigator)")) {
            sdkLogger.debug("Service workers are not supported in this browser. Push notifications will not work.")
            return Result.failure(UnsupportedOperationException("Service workers are not supported in this browser."))  // todo custom exception
        }
        if (js("!('PushManager' in window)")) {
            sdkLogger.debug("Push API is not supported in this browser. Push notifications will not work.")
            return Result.failure(UnsupportedOperationException("Push API is not supported in this browser.")) // todo custom exception
        }
        return try {
            val serviceWorkerOptions = getServiceWorkerOptions() ?: return Result.failure(
                IllegalStateException("Service worker options are not set.")
            )
            println("Registering service worker with options: ${JSON.stringify(serviceWorkerOptions)}")
            serviceWorkerManager.register(serviceWorkerOptions).onFailure {
                return Result.failure(
                    IllegalStateException(
                        "Service worker registration failed: ${it.message}",
                        it
                    )
                )
            }.onSuccess {
                serviceWorkerRegistration = it
            }
            webPermissionHandler.requestPushPermission()
            if (
                getPermissionState() != PermissionState.Granted
            ) {
                return Result.failure(IllegalStateException("Push permission was not granted."))
            }
            val options = createPushSubscriptionOptions(serviceWorkerOptions)
            println("Subscribing to push notifications with options: ${JSON.stringify(options)}")
            val pushSubscription = getServiceWorkerRegistration()?.pushManager?.subscribe(options)
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

    override suspend fun getServiceWorkerRegistration(): ServiceWorkerRegistration? {
        return try {
            serviceWorkerRegistration ?: getServiceWorkerOptions()?.let { options ->
                serviceWorkerManager.register(options).getOrNull()
                    .also { serviceWorkerRegistration = it }
                this.serviceWorkerRegistration?.update()
            }
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Failed to get service worker registration", e)
            null
        }
    }

    override suspend fun getPermissionState(): PermissionState {
        return getServiceWorkerRegistration()?.pushManager?.let { pushManager ->
            enumValueOf<PermissionState>(
                pushManager.permissionState().toString().replaceFirstChar { it.titlecase() })
        } ?: PermissionState.Denied
    }

    private suspend fun getServiceWorkerOptions(): ServiceWorkerOptions? {
        val config = sdkContext.getSdkConfig() as JsEngagementCloudSDKConfig?
        println("Getting service worker options from SDK config: ${JSON.stringify(config)}")
        return config?.serviceWorkerOptions
    }

    override suspend fun unsubscribe(): Result<Unit> = runCatchingWithoutCancellation {
        getPushSubscription()?.unsubscribe()
        getServiceWorkerRegistration()?.unregister()
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
