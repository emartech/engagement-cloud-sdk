package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Dismiss")
data class DismissActionModel(
    override val id: String,
    override val reporting: String,
    override val title: String,
    override val type: String
): ActionModel
