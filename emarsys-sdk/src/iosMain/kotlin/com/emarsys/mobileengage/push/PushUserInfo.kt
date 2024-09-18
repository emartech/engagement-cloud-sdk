package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushUserInfo(
    val ems: PushUserInfoEms? = null
)

@Serializable
data class PushUserInfoEms(
    val multichannelId: String? = null,
    val inapp: PushUserInfoInApp? = null,
    @SerialName("default_action")
    val defaultAction: PresentableActionModel? = null,
    val actions: List<PresentableActionModel>? = null
)


@Serializable
data class PushUserInfoInApp(
    @SerialName("campaign_id")
    val campaignId: String,
    val url: String
)