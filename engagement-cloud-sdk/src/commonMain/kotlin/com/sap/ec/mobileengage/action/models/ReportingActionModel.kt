package com.sap.ec.mobileengage.action.models

import com.sap.ec.mobileengage.inapp.presentation.InAppType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface ReportingActionModel

internal interface PushButtonClickedActionModel : ReportingActionModel {
    val trackingInfo: String
}

internal interface InAppButtonClickedActionModel : ReportingActionModel {
    var trackingInfo: String
    var inAppType: InAppType

}

@Serializable
@SerialName("pushButtonClicked")
internal data class BasicPushButtonClickedActionModel(
    override val reporting: String = "",
    override val trackingInfo: String
) : BasicActionModel(), PushButtonClickedActionModel

@Serializable
@SerialName("inAppButtonClicked")
internal data class BasicInAppButtonClickedActionModel(
    override val reporting: String = "",
    override var trackingInfo: String = "",
    override var inAppType: InAppType = InAppType.OVERLAY

) : BasicActionModel(), InAppButtonClickedActionModel

@Serializable
@SerialName("notificationOpened")
internal data class NotificationOpenedActionModel(
    val reporting: String? = null,
    val trackingInfo: String
) : ActionModel, ReportingActionModel