package com.emarsys.mobileengage.action.models

import com.emarsys.networking.clients.embeddedMessaging.model.EmbeddedMessageAnimation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RichContentDisplayAction")
data class BasicRichContentDisplayActionModel(
    override val reporting: String,
    val url: String,
    val animation: EmbeddedMessageAnimation
) : BasicActionModel()