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
import kotlinx.serialization.json.put

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
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        attributes = buildJsonObject {
                            put("origin", JsonPrimitive(BUTTON_CLICK_ORIGIN))
                        }
                    )
                )
            }

            is BasicInAppButtonClickedActionModel -> {
                //TODO: follow up reporting changes
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.InApp.ButtonClicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        attributes = buildJsonObject {
                            put("origin", JsonPrimitive(BUTTON_CLICK_ORIGIN))
                        }
                    )
                )
            }

            is NotificationOpenedActionModel -> {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Push.Clicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        attributes = buildJsonObject { put("origin", "main") }
                    )
                )
            }
        }
    }
}