package com.sap.ec.iosNotificationService.models

import com.sap.ec.iosNotificationService.provider.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Dismiss")
data class DismissActionModel(
    override val id: String = UUIDProvider().provide().UUIDString,
    override val reporting: String,
    override val title: String,
    override val type: String
): ActionModel
