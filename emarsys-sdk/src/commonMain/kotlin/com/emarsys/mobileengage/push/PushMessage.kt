package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

interface PushMessage<T: PlatformData> {
    val messageId: String
    val title: String
    val body: String
    val iconUrlString: String?
    val imageUrlString: String?
    val data: PushData<T>
}

interface PlatformData

@Serializable
data class PushData<T: PlatformData>(
    val silent: Boolean = false,
    val sid: String,
    val campaignId: String,
    val platformData: T,
    val defaultTapAction: BasicActionModel? = null,
    val actions: List<PresentableActionModel>? = null,
    val pushToInApp: PushToInApp? = null,
    val rootParams: JsonObject? = null,
    val u: JsonObject? = null
)
