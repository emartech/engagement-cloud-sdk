package com.emarsys.iosNotificationService.models

class OpenUrlActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val url: String
): ActionModel
