package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName

@SerialName("MECustomEvent")
data class CustomEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>? = null
): ActionModel