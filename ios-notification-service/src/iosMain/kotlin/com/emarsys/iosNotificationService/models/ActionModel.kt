package com.emarsys.iosNotificationService.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface ActionModel {
    val id: String
    val title: String
    val type: String
}
