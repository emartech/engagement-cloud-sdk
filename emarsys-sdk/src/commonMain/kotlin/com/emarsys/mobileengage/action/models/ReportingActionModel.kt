package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ReportingActionModel

interface PushButtonClickedActionModel : ReportingActionModel {
    val trackingInfo: String
}

interface InAppButtonClickedActionModel : ReportingActionModel {
    var trackingInfo: String
}

@Serializable
@SerialName("pushButtonClicked")
data class BasicPushButtonClickedActionModel(
    override val reporting: String,
    override val trackingInfo: String
) : BasicActionModel(), PushButtonClickedActionModel

@Serializable
@SerialName("inAppButtonClicked")
data class BasicInAppButtonClickedActionModel(
    override val reporting: String,
    override var trackingInfo: String = "",
) : BasicActionModel(), InAppButtonClickedActionModel

@Serializable
@SerialName("notificationOpened")
data class NotificationOpenedActionModel(
    val reporting: String? = null,
    val trackingInfo: String
) : ActionModel, ReportingActionModel