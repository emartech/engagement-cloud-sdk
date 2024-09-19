package com.emarsys

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.push.PushMessageMapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import org.w3c.dom.BroadcastChannel
import web.serviceworker.ServiceWorkerGlobalScope

external var self: ServiceWorkerGlobalScope

@JsName("EmarsysServiceWorker")
class EmarsysServiceWorker(
    private val pushMessagePresenter: PushMessagePresenter,
    private val pushMessageMapper: PushMessageMapper,
    private val coroutineScope: CoroutineScope,
    private val sdkLogger: Logger
) {
    private val readyBroadcastChannel = BroadcastChannel("emarsys-sdk-ready-channel")

    init {
        readyBroadcastChannel.postMessage("READY")
    }

    fun onPush(event: String): Promise<Any?> {
        return Promise { resolve, reject ->
            coroutineScope.promise {
                try {
                    pushMessageMapper.map(event)?.let {
                        pushMessagePresenter.present(it)
                    }
                } catch (exception: Exception) {
                    sdkLogger.error("EmarsysServiceWorker - onPush", exception)
                    throw exception
                }
            }
                .then { resolve(it) }
                .catch { reject(it) }
        }
    }
}