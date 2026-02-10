package com.emarsys.mobileengage.inapp.iframe

import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import web.events.EventHandler
import web.messaging.MessageChannel

internal class MessageChannelProvider(
    private val eventActionFactory: EventActionFactoryApi,
    private val applicationScope: CoroutineScope,
    private val json: Json
) : MessageChannelProviderApi {
    private companion object {
        const val CONNECTED_EVENT = "connected"
    }

    override fun provide(inAppMessage: InAppMessage): MessageChannel {
        return registerMessageHandler(MessageChannel(), inAppMessage)
    }

    private fun registerMessageHandler(
        messageChannel: MessageChannel,
        message: InAppMessage
    ): MessageChannel {
        messageChannel.port1.onmessage = EventHandler { messageEvent ->
            if (messageEvent.data == CONNECTED_EVENT) {
                console.log("Iframe content loaded.")
                return@EventHandler
            }

            val actionModel =
                json.decodeFromString<BasicActionModel>(JSON.stringify(messageEvent.data))
                    .amend(message)

            applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                eventActionFactory.create(actionModel).invoke()
            }
        }
        return messageChannel
    }

    private fun BasicActionModel.amend(message: InAppMessage): BasicActionModel {
        if (this is DismissActionModel) {
            this.dismissId = message.dismissId
        }

        if (this is BasicInAppButtonClickedActionModel) {
            this.trackingInfo = message.trackingInfo
        }

        return this
    }
}