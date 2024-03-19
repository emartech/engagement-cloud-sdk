package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class BadgeCountActionModel(
    override val type: String,
    val method: String,
    val value: Int
): ActionModel(), PushActionModel
