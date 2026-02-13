package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.UUIDProvider
import com.emarsys.mobileengage.inapp.presentation.InAppType
import kotlinx.serialization.Serializable

@Serializable
data class InAppMessage(
    val dismissId: String = UUIDProvider().provide(),
    val type: InAppType = InAppType.OVERLAY,
    val trackingInfo: String,
    val content: String,
)
