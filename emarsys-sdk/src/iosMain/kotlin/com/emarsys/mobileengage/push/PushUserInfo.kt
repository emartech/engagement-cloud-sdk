package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

@Serializable
data class PresentablePushUserInfo(
    val ems: PresentablePushUserInfoEms? = null,
    val u: U? = null
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class PresentablePushUserInfoEms(
    val multichannelId: String? = null,
    val inapp: PushUserInfoInApp? = null,
    val sid: String? = null,
    @JsonNames("default_action")
    val defaultAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class BasicPushUserInfo(
    val ems: BasicPushUserInfoEms,
    val u: U? = null
)

@Serializable
data class BasicPushUserInfoEms(
    val multichannelId: String,
    val inapp: PushUserInfoInApp? = null,
    val sid: String? = null,
    val actions: List<BasicActionModel>? = null,
    val badgeCount: BadgeCount? = null
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

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Map<String, Any>.toBasicPushUserInfo(json: Json): BasicPushUserInfo {
    val userInfoString = NSString.create(
        NSJSONSerialization.dataWithJSONObject(
            this,
            NSJSONWritingPrettyPrinted,
            null
        )!!, NSUTF8StringEncoding
    ).toString()
    return json.decodeFromString<BasicPushUserInfo>(userInfoString)
}