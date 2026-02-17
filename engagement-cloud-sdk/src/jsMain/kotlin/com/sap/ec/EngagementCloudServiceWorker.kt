package com.sap.ec

import com.sap.ec.core.log.ConsoleLogger
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.mapper.Mapper
import com.sap.ec.mobileengage.push.PushMessagePresenter
import com.sap.ec.mobileengage.push.model.JsPushMessage
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import web.broadcast.BroadcastChannel
import web.serviceworker.ServiceWorkerGlobalScope

external var self: ServiceWorkerGlobalScope

@JsName("EngagementCloudServiceWorker")
class EngagementCloudServiceWorker(
    private val pushMessagePresenter: PushMessagePresenter,
    private val pushMessageWebV1Mapper: Mapper<String, JsPushMessage>,
    private val onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel,
    private val json: StringFormat,
    private val logger: ConsoleLogger
) {

    suspend fun onPush(event: String): JsPushMessage? {
        try {
            val pushMessage: JsPushMessage? = pushMessageWebV1Mapper.map(event)
            pushMessage?.let {
                pushMessagePresenter.present(it)
                pushMessage.badgeCount?.let { badgeCount ->
                    val badgeCountString = json.encodeToString(badgeCount)
                    onBadgeCountUpdateReceivedBroadcastChannel.postMessage(badgeCountString)
                }
            }
            return pushMessage
        } catch (exception: Exception) {
            logger.logToConsole(
                "EngagementCloudServiceWorker - onPush",
                LogLevel.Error,
                "Error processing push message",
                exception,
                JsonObject(emptyMap())
            )
            throw exception
        }
    }
}