package com.emarsys.mobileengage.push.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMethod(
    val collapseId: String,
    val operation: NotificationOperation
)