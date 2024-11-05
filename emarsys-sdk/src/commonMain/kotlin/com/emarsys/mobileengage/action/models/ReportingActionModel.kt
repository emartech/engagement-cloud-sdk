package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ReportingActionModel

interface PushButtonClickedActionModel: ReportingActionModel {
    val sid: String
}

interface InAppButtonClickedActionModel: ReportingActionModel {
    val sid: String?
    val url: String?
}

@Serializable
@SerialName("pushButtonClicked")
data class BasicPushButtonClickedActionModel(
    val id: String,
    override val sid: String
): BasicActionModel(), PushButtonClickedActionModel

@Serializable
@SerialName("inAppButtonClicked")
data class BasicInAppButtonClickedActionModel(
    val id: String,
    override val sid: String? = null,
    override val url: String? = null
): BasicActionModel(), InAppButtonClickedActionModel

@Serializable
@SerialName("notificationOpened")
data class NotificationOpenedActionModel(
    val sid: String? = null
): BasicActionModel(), ReportingActionModel