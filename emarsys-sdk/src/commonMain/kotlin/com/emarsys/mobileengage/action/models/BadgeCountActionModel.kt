package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

interface BadgeCountActionModel {
    val method: String
    val value: Int
}

@Serializable
data class BasicBadgeCountActionModel(
    override val method: String,
    override val value: Int
): BasicActionModel(), BadgeCountActionModel
