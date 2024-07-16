package com.emarsys.iosNotificationService.models

data class DismissActionModel(
    override val id: String,
    override val title: String,
    override val type: String
): ActionModel
