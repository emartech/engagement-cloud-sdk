package com.emarsys.core.device.notification

import com.emarsys.core.device.ChannelSettings
import kotlinx.serialization.Serializable

@Serializable
data class AndroidNotificationSettings(
    val areNotificationsEnabled: Boolean,
    val importance: Int,
    val channelSettings: List<ChannelSettings>
)