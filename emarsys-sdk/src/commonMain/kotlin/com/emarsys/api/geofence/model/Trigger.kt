package com.emarsys.api.geofence.model

import com.emarsys.mobileengage.action.models.ActionModel

data class Trigger(
    val id: String,
    val type: TriggerType,
    val loiteringDelay: Int = 0,
    val action: ActionModel
)

enum class TriggerType {
    ENTER,
    EXIT,
    DWELLING;
}