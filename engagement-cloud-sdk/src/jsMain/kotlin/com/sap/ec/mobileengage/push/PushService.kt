package com.sap.ec.mobileengage.push

import JsEngagementCloudSDKConfig
import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import js.buffer.BufferSource
import js.promise.await
import js.typedarrays.toUint8Array
import kotlinx.browser.window
import web.navigator.navigator
import web.push.PushSubscriptionOptionsInit
import web.push.subscribe

//TODO: handle errors in registration and subscription
internal class PushService(
    private val pushServiceContext: PushServiceContextApi,
    private val storage: StringStorageApi,
    private val sdkLogger: Logger
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
                    pushServiceContext.registration?.pushManager?.subscribe(options)
                val pushToken =
                    pushSubscription?.let { subscription -> JSON.stringify(subscription) }
                pushToken?.let { token ->
                    storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, token)
                    pushServiceContext.isSubscribed = true
                }
            }
        } catch (e: Throwable) {
            sdkLogger.error("Push subscription failed", e)
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
