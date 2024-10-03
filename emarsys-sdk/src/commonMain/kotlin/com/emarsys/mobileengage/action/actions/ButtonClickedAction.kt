package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.BUTTON_CLICKED_EVENT_NAME
import com.emarsys.SdkConstants.BUTTON_CLICK_ORIGIN
import com.emarsys.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.ButtonClickedActionModel
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType

data class ButtonClickedAction(
    private val action: ButtonClickedActionModel,
    private val eventChannel: CustomEventChannelApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        when (action) {
            is BasicPushButtonClickedActionModel -> {
                eventChannel.send(
                    Event(
                        EventType.INTERNAL,
                        BUTTON_CLICKED_EVENT_NAME,
                        mapOf(
                            "button_id" to action.id,
                            "sid" to action.sid,
                            "origin" to BUTTON_CLICK_ORIGIN
                        )
                    )
                )
            }
            is BasicInAppButtonClickedActionModel -> {
                val attributes = mutableMapOf(
                    "buttonId" to action.id,
                    "campaignId" to action.campaignId
                )

                action.sid?.let { attributes["sid"] = it }
                action.url?.let { attributes["url"] = it }

                eventChannel.send(
                    Event(
                        EventType.INTERNAL,
                        IN_APP_BUTTON_CLICKED_EVENT_NAME,
                        attributes
                    )
                )
            }
        }
    }
}