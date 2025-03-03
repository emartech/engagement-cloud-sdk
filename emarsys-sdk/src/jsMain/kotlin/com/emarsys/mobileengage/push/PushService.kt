package com.emarsys.mobileengage.push

import com.emarsys.JsEmarsysConfig
import com.emarsys.ServiceWorkerOptions
import com.emarsys.api.push.PushConstants
import com.emarsys.core.storage.TypedStorageApi
import js.buffer.BufferSource
import js.typedarrays.Uint8Array
import kotlinx.browser.window
import web.navigator.navigator
import web.push.PushSubscriptionOptionsInit

//TODO: add logger instead of console log
class PushService(
    private val pushServiceContext: PushServiceContext,
    private val storage: TypedStorageApi<String?>
) : PushServiceApi {

    override suspend fun register(config: JsEmarsysConfig) {
        config.serviceWorkerOptions?.let {
            pushServiceContext.registration =
                navigator.serviceWorker.register(it.serviceWorkerPath)
            navigator.serviceWorker.ready.await()
        }
    }

    override suspend fun subscribeForPushMessages(config: JsEmarsysConfig) {
        try {
            config.serviceWorkerOptions?.let {
                val options = createPushSubscriptionOptions(it)
                val pushSubscription =
                    pushServiceContext.registration.pushManager.subscribe(options)
                val pushToken = JSON.stringify(pushSubscription.toJSON())
                storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, pushToken)
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
