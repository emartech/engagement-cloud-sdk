package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.PlatformData
import kotlinx.serialization.Serializable

@Serializable
data class JsPlatformData(val applicationCode: String) : PlatformData