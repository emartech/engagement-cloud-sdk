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