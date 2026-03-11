package com.sap.ec.mobileengage.action.actions

import com.sap.ec.SdkConstants.BUTTON_CLICK_ORIGIN
import com.sap.ec.SdkConstants.EMBEDDED_MESSAGING_BUTTON_CLICKED_EVENT_NAME
import com.sap.ec.SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.action.models.ReportingActionModel
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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
                val eventName = if (action.inAppType == InAppType.EMBEDDED_MESSAGING)
                    EMBEDDED_MESSAGING_BUTTON_CLICKED_EVENT_NAME
                else
                    IN_APP_BUTTON_CLICKED_EVENT_NAME
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.InApp.ButtonClicked(
                        reporting = action.reporting,
                        trackingInfo = action.trackingInfo,
                        origin = BUTTON_CLICK_ORIGIN,
                        reportingName = eventName
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