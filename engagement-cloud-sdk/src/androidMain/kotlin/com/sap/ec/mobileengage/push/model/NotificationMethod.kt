package com.sap.ec.mobileengage.push.model

import com.sap.ec.mobileengage.push.NotificationOperation
import kotlinx.serialization.Serializable

@Serializable
data class NotificationMethod(
    val collapseId: String,
    val operation: NotificationOperation
)