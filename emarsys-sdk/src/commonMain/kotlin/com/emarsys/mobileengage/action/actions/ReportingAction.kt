package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.BUTTON_CLICK_ORIGIN
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.ReportingActionModel

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class ReportingAction(
    private val action: ReportingActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        when (action) {
            //TODO: follow up reporting changes
            is BasicPushButtonClickedActionModel -> {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Push.Clicked(
                        attributes = buildJsonObject {
                            put("buttonId", JsonPrimitive(action.id))
                            put("origin", JsonPrimitive(BUTTON_CLICK_ORIGIN))
                        }
                    )
                )
            }

            is BasicInAppButtonClickedActionModel -> {
                val attributes = mutableMapOf(
                    "buttonId" to action.id
                )

                //TODO: follow up reporting changes
//                action.sid?.let { attributes["sid"] = it }
                action.url?.let { attributes["url"] = it }

                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.InApp.ButtonClicked(
                        id = action.id,
                       attributes = buildJsonObject {
                            attributes.forEach { (key, value) ->
                                put(key, JsonPrimitive(value))
                            }
                       }
                    )
                )
            }

            is NotificationOpenedActionModel -> {
                val attributes = mutableMapOf(
                    "origin" to "main"
                )

                //TODO: follow up reporting changes
//                action.sid?.let { attributes["sid"] = it }

                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Push.Clicked(
                        attributes = buildJsonObject {
                            attributes.forEach { (key, value) ->
                                put(key, JsonPrimitive(value))
                            }
                        }
                    )
                )
            }
        }
    }
}