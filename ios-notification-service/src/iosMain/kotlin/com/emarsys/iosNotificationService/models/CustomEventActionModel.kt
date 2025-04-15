package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MECustomEvent")
data class CustomEventActionModel(
    override val id: String,
    override val reporting: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>? = null
): ActionModel