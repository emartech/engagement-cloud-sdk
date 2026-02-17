package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import kotlinx.serialization.Serializable

@Serializable
data class InAppMessage(
    val dismissId: String = UUIDProvider().provide(),
    val type: InAppType = InAppType.OVERLAY,
    val trackingInfo: String,
    val content: String,
)
