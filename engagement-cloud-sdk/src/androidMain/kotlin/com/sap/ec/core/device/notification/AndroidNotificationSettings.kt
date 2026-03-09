package com.sap.ec.core.device.notification

import com.sap.ec.core.device.ChannelSettings
import kotlinx.serialization.Serializable

@Serializable
internal data class AndroidNotificationSettings(
    val areNotificationsEnabled: Boolean,
    val importance: Int,
    val channelSettings: List<ChannelSettings>
)