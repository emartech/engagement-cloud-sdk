package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName

@SerialName("OpenExternalUrl")
class OpenUrlActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val url: String
): ActionModel
