package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName

@SerialName("Dismiss")
data class DismissActionModel(
    override val id: String,
    override val title: String,
    override val type: String
): ActionModel
