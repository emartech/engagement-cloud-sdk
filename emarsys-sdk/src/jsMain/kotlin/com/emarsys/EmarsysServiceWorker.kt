package com.emarsys

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.push.PushMessageMapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.ServiceWorkerRegistrationWrapper
import com.emarsys.util.JsonUtil
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import web.push.PushEvent
import web.serviceworker.ServiceWorkerGlobalScope

external var self: ServiceWorkerGlobalScope

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysServiceWorker")
class EmarsysServiceWorker {

    // TODO creating these in the dependency container
    private val pushMessagePresenter = PushMessagePresenter(ServiceWorkerRegistrationWrapper())
    private val pushMessageMapper = PushMessageMapper(JsonUtil.json, SdkLogger(ConsoleLogger()))
    private val scope = CoroutineScope(SupervisorJob())

    fun onPush(event: PushEvent): Promise<Any?> {
        return Promise { resolve, reject ->
            scope.promise {
                val jsPushMessage = pushMessageMapper.map(JSON.stringify(event.data?.json()))
                jsPushMessage?.let {
                    pushMessagePresenter.present(it)
                    null
                }
            }
                .then { resolve(it) }
                .catch { reject(it) }
        }
    }
}