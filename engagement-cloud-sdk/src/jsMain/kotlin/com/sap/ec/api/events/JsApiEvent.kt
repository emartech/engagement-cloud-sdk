@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.events

@JsExport
@JsName("EngagementCloudEvent")
sealed external interface JsApiEvent {
    val type: String
    val id: String
}

@JsExport
@JsName("BadgeCount")
external interface JsBadgeCountEvent : JsApiEvent {
    override val type: String
    override val id: String
    val badgeCount: Int
    val method: String
}

@JsExport
@JsName("AppEvent")
external interface JsAppEvent : JsApiEvent {
    override val type: String
    override val id: String
    val name: String
    val payload: JSON
}

@JsExport
@JsName("EngagementCloudEventTypes")
object JsApiEventTypes {
    const val APP_EVENT = "app_event"
    const val BADGE_COUNT = "badge_count"
}
