package com.emarsys.mobileengage.push

import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushInternalApi
import js.buffer.BufferSource
import js.promise.await
import js.typedarrays.Uint8Array
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationPermission
import web.navigator.navigator
import web.push.PushSubscriptionOptionsInit

//TODO: add logger instead of console log
class PushService(
    private val applicationServerKey: String,
    private val serviceWorkerPath: String,
    private val pushApi: PushInternalApi
) {

    suspend fun register() {
        if (Notification.requestPermission().await() != NotificationPermission.GRANTED) return
        subscribeForPushReceiving()
        subscribeForPushMessages()
    }

    private fun subscribeForPushMessages() {
        try {
            navigator.serviceWorker.onmessage = {
                console.log("MESSAGE: ${JSON.stringify(it.data)}")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun subscribeForPushReceiving() {
        try {
            val registration = navigator.serviceWorker.register(serviceWorkerPath).await()
            val options = createPushSubscriptionOptions()
            navigator.serviceWorker.ready.await()
            val pushToken = registration.pushManager.subscribe(options).await()
            pushApi.registerPushToken(pushToken.toString())
        } catch (throwable: Throwable) {
            console.log("service worker registration failed: $throwable")
        }
    }

    private fun createPushSubscriptionOptions(): PushSubscriptionOptionsInit =
        js("{}").unsafeCast<PushSubscriptionOptionsInit>().apply {
            this.applicationServerKey = urlBase64ToUint8Array(this@PushService.applicationServerKey)
            this.userVisibleOnly = true
        }

    private fun urlBase64ToUint8Array(base64String: String): BufferSource {
        val padding = "=".repeat((4 - base64String.length % 4) % 4);
        val base64 = (base64String + padding)
            .replace('-', '+')
            .replace('_', '/')


        val rawData = window.atob(base64)
        val outputArray = Uint8Array(rawData.length)

        for (i in rawData.indices) {
            outputArray[i] = rawData[i].code.toByte()
        }
        return outputArray
    }

}
