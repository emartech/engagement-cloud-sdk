package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ButtonClickedActionModel

interface PushButtonClickedActionModel: ButtonClickedActionModel {
    val id: String
    val sid: String
}

interface InAppButtonClickedActionModel: ButtonClickedActionModel {
    val id: String
    val campaignId: String
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
    override val campaignId: String
): BasicActionModel(), InAppButtonClickedActionModel