package com.emarsys.push

import com.emarsys.api.push.PushApi
import js.buffer.BufferSource
import js.promise.await
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
    private val pushApi: PushApi
) {

    suspend fun register() {
        if (Notification.requestPermission().await() != NotificationPermission.GRANTED) return
        subscribeForPushReceiving()
        subscribeForPushMessages()
    }

    private fun subscribeForPushMessages() {
//        navigator.serviceWorker.addEventListener(PushEvent.PUSH, {
//            val notification = it.data?.json() //TODO: convert to universal emarsys push data format, add it to push handler, which was given as a constructor dependency
//        })


//        navigator.serviceWorker.addEventListener(MessageEvent.MESSAGE, {
//
//
//
//        })
    }

    private suspend fun subscribeForPushReceiving() {
        try {
            val registration = navigator.serviceWorker.register(serviceWorkerPath).await()
            console.log("service worker registered")
            val options = createPushSubscriptionOptions()
            val pushToken = registration.pushManager.subscribe(options).await()
            pushApi.registerPushToken(pushToken.toString())
        } catch (throwable: Throwable) {
            console.log("service worker registration failed")
        }
    }

    private fun createPushSubscriptionOptions(): PushSubscriptionOptionsInit =
        js("{}").unsafeCast<PushSubscriptionOptionsInit>().apply {
            this.applicationServerKey =
                this@PushService.applicationServerKey.unsafeCast<BufferSource>() //TODO BASE64 to BufferSource
            this.userVisibleOnly = true
        }

}
