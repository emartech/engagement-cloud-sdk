package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName

@SerialName("MEAppEvent")
data class AppEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>? = null
): ActionModel
