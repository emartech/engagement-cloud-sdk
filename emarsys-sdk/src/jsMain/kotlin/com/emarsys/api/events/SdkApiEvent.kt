package com.emarsys.api.events

@OptIn(ExperimentalJsExport::class)
@JsExport
sealed external interface SdkApiEvent {
    val type: String
    val id: String
    val name: String
    val attributes: JSON
}

@OptIn(ExperimentalJsExport::class)
@JsExport
external interface SdkApiBadgeCountEvent : SdkApiEvent {
    override val type: String
    override val id: String
    val badgeCount: Int
    val method: String
}

@OptIn(ExperimentalJsExport::class)
@JsExport
external interface SdkApiAppEvent : SdkApiEvent {
    override val type: String
    override val id: String
    override val name: String
    override val attributes: JSON
}