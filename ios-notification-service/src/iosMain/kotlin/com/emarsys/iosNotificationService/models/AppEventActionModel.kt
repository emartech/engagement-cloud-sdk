package com.emarsys.iosNotificationService.models

import com.emarsys.iosNotificationService.provider.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MEAppEvent")
data class AppEventActionModel(
    override val id: String = UUIDProvider().provide().UUIDString,
    override val reporting: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>? = null
): ActionModel
