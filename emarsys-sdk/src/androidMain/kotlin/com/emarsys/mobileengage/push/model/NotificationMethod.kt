package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.NotificationOperation
import kotlinx.serialization.Serializable

@Serializable
data class NotificationMethod(
    val collapseId: String,
    val operation: NotificationOperation
)