package com.emarsys.iosNotificationService.models

import com.emarsys.iosNotificationService.provider.UUIDProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("OpenExternalUrl")
class OpenUrlActionModel(
    override val id: String = UUIDProvider().provide().UUIDString,
    override val reporting: String,
    override val title: String,
    override val type: String,
    val url: String
): ActionModel
