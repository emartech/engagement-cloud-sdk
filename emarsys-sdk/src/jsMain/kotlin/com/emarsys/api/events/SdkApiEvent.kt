@file:OptIn(ExperimentalJsExport::class)

package com.emarsys.api.events

@JsExport
sealed external interface SdkApiEvent {
    val type: String
    val id: String
}

@JsExport
external interface SdkApiBadgeCountEvent : SdkApiEvent {
    override val type: String
    override val id: String
    val badgeCount: Int
    val method: String
}

@JsExport
external interface SdkApiAppEvent : SdkApiEvent {
    override val type: String
    override val id: String
    val name: String
    val attributes: JSON
}

@JsExport
object SdkApiEventTypes {
    const val SDK_APP_EVENT = "app_event"
    const val SDK_BADGE_COUNT_EVENT = "badge_count"
}
