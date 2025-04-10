package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import kotlinx.serialization.Serializable

interface PlatformData

interface PushMessage<T : PlatformData> {
    val trackingInfo: String
    val platformData: T
    val badgeCount: BadgeCount?
}

interface DisplayablePush {
    val displayableData: DisplayableData?
}
interface ActionablePush<A: ActionModel> {
    val actionableData: ActionableData<A>?
}

@Serializable
data class DisplayableData(
    val title: String,
    val body: String,
    val iconUrlString: String? = null,
    val imageUrlString: String? = null
)

@Serializable
data class ActionableData<A: ActionModel>(
    val actions: List<A>? = emptyList<A>(),
    val defaultTapAction: BasicActionModel? = null,
    val pushToInApp: PushToInApp? = null
)
