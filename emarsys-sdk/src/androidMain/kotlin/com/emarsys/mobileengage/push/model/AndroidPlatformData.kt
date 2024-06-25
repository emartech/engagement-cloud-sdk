package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.PlatformData
import kotlinx.serialization.Serializable

@Serializable
data class AndroidPlatformData(
    val channelId: String
): PlatformData