package com.sap.ec.mobileengage.action.models

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.Serializable

@InternalSdkApi
@Serializable
data class BadgeCount(val method: BadgeCountMethod, val value: Int)

@InternalSdkApi
enum class BadgeCountMethod {
    ADD,
    SET
}