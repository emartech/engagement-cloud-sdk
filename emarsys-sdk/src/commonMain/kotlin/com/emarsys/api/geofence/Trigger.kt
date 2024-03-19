package com.emarsys.api.geofence

import com.emarsys.mobileengage.action.models.ActionModel


data class Trigger(
    val id: String,
    val type: Enum<TriggerType>,
    val loiteringDelay: Int = 0,
    val action: ActionModel
)

enum class TriggerType {
    ENTER,
    EXIT,
    DWELLING;
}