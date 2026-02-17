package com.sap.ec.mobileengage.inapp.iframe

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.DismissActionModel
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.toIframeId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import web.events.EventHandler
import web.messaging.MessageChannel

internal class MessageChannelProvider(
    private val eventActionFactory: EventActionFactoryApi,
    private val applicationScope: CoroutineScope,
    private val iframeContainerResizer: IframeContainerResizerApi,
    private val logger: Logger,
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

            try {
                if (handleResizeEvent(JSON.stringify(messageEvent.data), message.dismissId)) {
                    return@EventHandler
                }
                val actionModel =
                    json.decodeFromString<BasicActionModel>(JSON.stringify(messageEvent.data))
                        .amend(message)

                applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    eventActionFactory.create(actionModel).invoke()
                }
            } catch (exception: Throwable) {
                applicationScope.launch {
                    logger.error("Failed to parse actionModel from messageEvent data.", exception)
                }
            }
        }
        return messageChannel
    }

    private fun BasicActionModel.amend(message: InAppMessage): BasicActionModel {
        if (this is DismissActionModel) {
            this.dismissId = message.dismissId
        } else if (this is BasicInAppButtonClickedActionModel) {
            this.trackingInfo = message.trackingInfo
        }

        return this
    }

    private fun handleResizeEvent(messageData: String, dismissId: String): Boolean {
        return try {
            val resizeMessage = json.decodeFromString<IframeResizeMessage>(messageData)
            iframeContainerResizer.resize(dismissId.toIframeId(), resizeMessage.height)
            true
        } catch (_: Throwable) {
            false
        }
    }
}

@Serializable
data class IframeResizeMessage(val type: String, val height: Int)