package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

interface PushMessage<T: PlatformData> {
    val messageId: String
    val title: String
    val body: String
    val iconUrlString: String?
    val imageUrlString: String?
    val data: PushData<T>?
}

interface PlatformData

@Serializable
data class PushData<T: PlatformData>(
    val silent: Boolean = false,
    val sid: String,
    val campaignId: String,
    val platformData: T? = null,
    val defaultAction: PresentableActionModel? = null,
    val defaultTapAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val inApp: InApp? = null,
    val rootParams: JsonObject? = null,
    val u: JsonObject? = null
)

@Serializable
data class InApp(
    val campaignId: String,
    val urlString: String,
    val ignoreViewedEvent: Boolean? = null
)
