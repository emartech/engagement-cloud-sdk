package com.sap.ec.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestPushPermission")
data class RequestPushPermissionActionModel(override val reporting: String = "") : BasicActionModel()
