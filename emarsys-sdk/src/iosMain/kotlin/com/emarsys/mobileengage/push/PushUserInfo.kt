package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class PushUserInfo(
    val ems: PushUserInfoEms? = null,
    val u: U? = null
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class PushUserInfoEms(
    val multichannelId: String? = null,
    val inapp: PushUserInfoInApp? = null,
    val sid: String? = null,
    @JsonNames("default_action")
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null
)

@Serializable
data class PushUserInfoInApp(
    @SerialName("campaign_id")
    val campaignId: String,
    val url: String
)

@Serializable
data class U(
    @SerialName("product-id")
    val productId: String? = null,
    val sid: String? = null
)