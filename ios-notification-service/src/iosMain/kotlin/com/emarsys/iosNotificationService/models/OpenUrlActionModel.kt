package com.emarsys.iosNotificationService.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("OpenExternalUrl")
class OpenUrlActionModel(
    override val id: String,
    override val reporting: String,
    override val title: String,
    override val type: String,
    val url: String
): ActionModel
