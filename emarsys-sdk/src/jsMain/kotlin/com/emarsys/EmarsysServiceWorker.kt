package com.emarsys

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.LogLevel
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.model.JsPushMessage
import js.promise.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import web.broadcast.BroadcastChannel
import web.serviceworker.ServiceWorkerGlobalScope

external var self: ServiceWorkerGlobalScope

@JsName("EmarsysServiceWorker")
class EmarsysServiceWorker(
    private val pushMessagePresenter: PushMessagePresenter,
    private val pushMessageWebV1Mapper: Mapper<String, JsPushMessage>,
    private val onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel,
    private val json: StringFormat,
    private val coroutineScope: CoroutineScope,
    private val logger: ConsoleLogger
) {

    fun onPush(event: String): Promise<Any?> {
        return Promise { resolve, reject ->
            coroutineScope.promise {
                try {
                    val pushMessage: JsPushMessage? = pushMessageWebV1Mapper.map(event)
                    pushMessage?.let {
                        pushMessagePresenter.present(it)
                        pushMessage.badgeCount?.let { badgeCount ->
                            val badgeCountString = json.encodeToString(badgeCount)
                            onBadgeCountUpdateReceivedBroadcastChannel.postMessage(badgeCountString)
                        }
                    }
                } catch (exception: Exception) {
                    logger.logToConsole(
                        "EmarsysServiceWorker - onPush",
                        LogLevel.Error,
                        "Error processing push message",
                        exception,
                        JsonObject(emptyMap())
                    )
                    throw exception
                }
            }
                .then { resolve(it) }
                .catch { reject(it) }
        }
    }
}