package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ReportingActionModel

interface PushButtonClickedActionModel: ReportingActionModel {
    val id: String
    val sid: String
}

interface InAppButtonClickedActionModel: ReportingActionModel {
    val id: String
    val campaignId: String
    val sid: String?
    val url: String?
}

@Serializable
@SerialName("pushButtonClicked")
data class BasicPushButtonClickedActionModel(
    override val id: String,
    override val sid: String
): BasicActionModel(), PushButtonClickedActionModel

@Serializable
@SerialName("inAppButtonClicked")
data class BasicInAppButtonClickedActionModel(
    override val id: String,
    override val campaignId: String,
    override val sid: String? = null,
    override val url: String? = null
): BasicActionModel(), InAppButtonClickedActionModel