package com.sap.ec

import com.sap.ec.core.log.ConsoleLogger
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.mapper.Mapper
import com.sap.ec.mobileengage.push.presentation.PushMessagePresenter
import com.sap.ec.mobileengage.push.model.JsPushMessage
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import web.broadcast.BroadcastChannel
import web.serviceworker.ServiceWorkerGlobalScope

@InternalSdkApi
external var self: ServiceWorkerGlobalScope

@InternalSdkApi
@JsName("EngagementCloudServiceWorker")
class EngagementCloudServiceWorker(
    private val pushMessagePresenter: PushMessagePresenter,
    private val pushMessageWebV2Mapper: Mapper<String, JsPushMessage>,
    private val onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel,
    private val json: StringFormat,
    private val logger: ConsoleLogger
) {

    suspend fun onPush(event: String): JsPushMessage? {
        try {
            println("PUSH EVENT RECEIVED: $event")
            val pushMessage: JsPushMessage? = pushMessageWebV2Mapper.map(event)
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