package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.inapp.PushToInApp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RemoteWebPushMessage(
    val title: String? = null,
    val message: String,
    val messageData: WebPushMessageData
)

@Serializable
data class WebPushMessageData(
    val id: String,
    val sid: String,
    val applicationCode: String,
    val treatments: JsonObject,
    val notificationSettings: WebPushNotificationSettings,
    val inApp: PushToInApp? = null
)

@Serializable
data class WebPushNotificationSettings(
    val icon: String? = null,
    val link: String? = null,
    val image: String? = null,
    val actions: List<RemoteWebPushAction>? = null,
    val badgeCount: BadgeCount? = null
)

@Serializable
data class RemoteWebPushAction(
    val id: String,
    val title: String,
    val url: String
)
