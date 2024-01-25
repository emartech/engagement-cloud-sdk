package com.emarsys.api.geofence

import com.emarsys.api.action.ActionModel


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