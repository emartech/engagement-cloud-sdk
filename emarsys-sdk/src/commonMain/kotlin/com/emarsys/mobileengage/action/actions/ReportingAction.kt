package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.BUTTON_CLICK_ORIGIN
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.ReportingActionModel

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class ReportingAction(
    private val action: ReportingActionModel,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        when (action) {
            is BasicPushButtonClickedActionModel -> {
                sdkEventFlow.emit(
                    SdkEvent.Internal.Push.Clicked(
                        buildJsonObject {
                            put("buttonId", JsonPrimitive(action.id))
                            put("sid", JsonPrimitive(action.sid))
                            put("origin", JsonPrimitive(BUTTON_CLICK_ORIGIN))
                        }
                    )
                )
            }

            is BasicInAppButtonClickedActionModel -> {
                val attributes = mutableMapOf(
                    "buttonId" to action.id
                )

                action.sid?.let { attributes["sid"] = it }
                action.url?.let { attributes["url"] = it }

                sdkEventFlow.emit(
                    SdkEvent.Internal.InApp.ButtonClicked(
                        buildJsonObject {
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

                action.sid?.let { attributes["sid"] = it }

                sdkEventFlow.emit(
                    SdkEvent.Internal.Push.Clicked(
                        buildJsonObject {
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