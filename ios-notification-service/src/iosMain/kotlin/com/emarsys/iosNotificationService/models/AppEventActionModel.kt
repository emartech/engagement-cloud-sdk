package com.emarsys.iosNotificationService.models

data class AppEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>? = null
): ActionModel
