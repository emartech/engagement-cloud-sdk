package com.emarsys

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.push.PushMessageMapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import web.broadcast.BroadcastChannel
import web.serviceworker.ServiceWorkerGlobalScope

external var self: ServiceWorkerGlobalScope

@JsName("EmarsysServiceWorker")
class EmarsysServiceWorker(
    private val pushMessagePresenter: PushMessagePresenter,
    private val pushMessageMapper: PushMessageMapper,
    private val sdkLogger: Logger
) {
    private val scope = CoroutineScope(SupervisorJob())
    private val pushBroadcastChannel = BroadcastChannel("emarsys-service-worker-push-channel")

    init {
        pushBroadcastChannel.onmessage = {
            onPush(it.data as String)
        }
    }

    fun onPush(event: String): Promise<Any?> {
        return Promise { resolve, reject ->
            scope.promise {
                try {
                    pushMessageMapper.map(event)?.let {
                        pushMessagePresenter.present(it)
                    }
                } catch (exception: Exception) {
                    sdkLogger.error("EmarsysServiceWorker - onPush", exception)
                }
            }
                .then { resolve(it) }
                .catch { reject(it) }
        }
    }
}