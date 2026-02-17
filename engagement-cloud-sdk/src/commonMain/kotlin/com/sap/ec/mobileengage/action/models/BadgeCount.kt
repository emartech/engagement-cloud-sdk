package com.sap.ec.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class BadgeCount(val method: BadgeCountMethod, val value: Int)

enum class BadgeCountMethod {
    ADD,
    SET
}