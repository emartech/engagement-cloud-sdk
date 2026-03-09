package com.sap.ec.mobileengage.action.models

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.Serializable

//needs to be exposed for ServiceWorker
@InternalSdkApi
@Serializable
 data class BadgeCount(val method: BadgeCountMethod, val value: Int)

//needs to be exposed for ServiceWorker
@InternalSdkApi
enum class BadgeCountMethod {
    ADD,
    SET
}