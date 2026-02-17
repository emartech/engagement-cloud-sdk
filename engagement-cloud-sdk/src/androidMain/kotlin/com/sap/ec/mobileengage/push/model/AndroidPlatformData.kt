package com.sap.ec.mobileengage.push.model

import com.sap.ec.mobileengage.push.PlatformData
import kotlinx.serialization.Serializable

@Serializable
data class AndroidPlatformData(
    val channelId: String,
    val notificationMethod: NotificationMethod,
    val style: NotificationStyle? = null
) : PlatformData