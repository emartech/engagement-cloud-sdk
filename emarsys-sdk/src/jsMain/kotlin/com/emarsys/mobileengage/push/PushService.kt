package com.emarsys.mobileengage.push

import com.emarsys.EmarsysConfig
import com.emarsys.api.push.PushConstants
import com.emarsys.api.push.PushInternalApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import js.buffer.BufferSource
import js.promise.await
import js.typedarrays.Uint8Array
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationPermission
import web.navigator.navigator
import web.push.PushMessageData
import web.push.PushSubscriptionOptionsInit

//TODO: add logger instead of console log
class PushService(
    private val pushServiceContext: PushServiceContext,
    private val pushApi: PushInternalApi,
    private val pushMessageMapper: PushMessageMapper,
    private val pushPresenter: PushPresenter<JsPlatformData, JsPushMessage>,
    private val storage: TypedStorageApi<String?>,
    private val sdkDispatcher: CoroutineDispatcher
) : PushServiceApi {

    override suspend fun register(config: EmarsysConfig) {
        if (Notification.requestPermission().await() != NotificationPermission.GRANTED) return
        subscribeForPushReceiving(config)
        subscribeForPushMessages()
    }

    private fun subscribeForPushMessages() {
        try {
            navigator.serviceWorker.onmessage = {
                CoroutineScope(sdkDispatcher).launch {
                    val pushMessage = pushMessageMapper.map((it.data as PushMessageData).text())
                    pushMessage?.let { pushPresenter.present(it) }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun subscribeForPushReceiving(config: EmarsysConfig) {
        try {
            val serviceWorkerPath = "/ems-service-worker.js" // TODO: config.serviceWorkerPath
            pushServiceContext.registration =
                navigator.serviceWorker.register(serviceWorkerPath).await()
            val options = createPushSubscriptionOptions(config)
            navigator.serviceWorker.ready.await()
            val pushToken = pushServiceContext.registration.pushManager.subscribe(options).await()
            storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, pushToken.toJSON().toString())
        } catch (throwable: Throwable) {
            console.log("service worker registration failed: $throwable")
        }
    }

    private fun createPushSubscriptionOptions(config: EmarsysConfig): PushSubscriptionOptionsInit {
        val applicationServerKey =
            "BDa49_IiPdIo2Kda5cATItp81sOaYg-eFFISMdlSXatDAIZCdtAxUuMVzXo4M2MXXI0sUYQzQI7shyNkKgwyD_I" // TODO: config.applicationServerKey
        return js("{}").unsafeCast<PushSubscriptionOptionsInit>().apply {
            this.applicationServerKey = urlBase64ToUint8Array(applicationServerKey)
            this.userVisibleOnly = true
        }
    }

    private fun urlBase64ToUint8Array(base64String: String): BufferSource {
        val padding = "=".repeat((4 - base64String.length % 4) % 4)
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
