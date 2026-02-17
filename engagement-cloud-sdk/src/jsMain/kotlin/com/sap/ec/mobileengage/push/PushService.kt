package com.sap.ec.mobileengage.push

import JsEngagementCloudSDKConfig
import com.sap.ec.ServiceWorkerOptions
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.storage.StringStorageApi
import js.buffer.BufferSource
import js.promise.await
import js.typedarrays.Uint8Array
import kotlinx.browser.window
import web.navigator.navigator
import web.push.PushSubscriptionOptionsInit

//TODO: add logger instead of console log
//TODO: handle errors in registration and subscription
class PushService(
    private val pushServiceContext: PushServiceContextApi,
    private val storage: StringStorageApi
) : PushServiceApi {

    override suspend fun register(config: JsEngagementCloudSDKConfig) {
        config.serviceWorkerOptions?.let {
            pushServiceContext.registration =
                (navigator.serviceWorker.asDynamic()
                    .register(it.serviceWorkerPath) as js.promise.Promise<web.serviceworker.ServiceWorkerRegistration>).await()
            navigator.serviceWorker.ready.await()
        }
    }

    override suspend fun subscribeForPushMessages(config: JsEngagementCloudSDKConfig) {
        try {
            config.serviceWorkerOptions?.let {
                val options = createPushSubscriptionOptions(it)
                val pushSubscription =
                    pushServiceContext.registration?.asDynamic().pushManager?.subscribe(options)
                val pushToken =
                    pushSubscription?.let { subscription -> JSON.stringify(subscription) }
                pushToken?.let { token ->
                    storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, token)
                    pushServiceContext.isSubscribed = true
                }
            }
        } catch (e: Throwable) {
            console.log("push subscription failed: $e")
        }
    }

    private fun createPushSubscriptionOptions(serviceWorkerOptions: ServiceWorkerOptions): PushSubscriptionOptionsInit {
        val options = PushSubscriptionOptionsInit(
            applicationServerKey = urlBase64ToUint8Array(serviceWorkerOptions.applicationServerKey),
            userVisibleOnly = true
        )
        return options
    }

    private fun urlBase64ToUint8Array(base64String: String): BufferSource {
        val padding = "=".repeat((4 - base64String.length % 4) % 4);
        val base64 = (base64String + padding)
            .replace('-', '+')
            .replace('_', '/');

        val rawData = window.atob(base64);
        val outputArray = Uint8Array.fromBase64(rawData).unsafeCast<BufferSource>();

        return outputArray;
    }

}
