package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class BadgeCount(val method: Method, val value: Int)

enum class Method {
    ADD,
    SET
}