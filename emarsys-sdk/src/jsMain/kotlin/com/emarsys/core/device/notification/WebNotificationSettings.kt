package com.emarsys.core.device.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class WebNotificationSettings(
    val permissionState: PermissionState,
    val isServiceWorkerRegistered: Boolean,
    val isSubscribed: Boolean
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class PermissionState {
    @SerialName("granted") Granted,
    @SerialName("denied") Denied,
    @SerialName("prompt") Prompt
}