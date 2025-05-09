package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.BUTTON_CLICK_ORIGIN
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.ReportingActionModel
import com.emarsys.networking.clients.event.model.SdkEvent

internal data class ReportingAction(
    private val action: ReportingActionModel,
    private val sdkEventDistributor: SdkEventDistributorApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        when (action) {
            is BasicPushButtonClickedActionModel -> {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Push.Clicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        origin = BUTTON_CLICK_ORIGIN
                    )
                )
            }

            is BasicInAppButtonClickedActionModel -> {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.InApp.ButtonClicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        origin = BUTTON_CLICK_ORIGIN
                    )
                )
            }

            is NotificationOpenedActionModel -> {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Push.Clicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        origin = "main"
                    )
                )
            }
        }
    }
}