package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestPushPermission")
data object AskForPushPermissionActionModel : BasicActionModel()
