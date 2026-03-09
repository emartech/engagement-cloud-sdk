package com.sap.ec.mobileengage.push

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BasicActionModel
import kotlinx.serialization.Serializable

internal interface PlatformData

internal interface PushMessage<T : PlatformData> {
    val trackingInfo: String
    val platformData: T
    val badgeCount: BadgeCount?
}

internal interface DisplayablePush {
    val displayableData: DisplayableData?
}

internal interface ActionablePush<A: ActionModel> {
    val actionableData: ActionableData<A>?
}

//needs to be exposed for ServiceWorker
@InternalSdkApi
@Serializable
data class DisplayableData(
    val title: String,
    val body: String,
    val iconUrlString: String? = null,
    val imageUrlString: String? = null
)

//needs to be exposed for ServiceWorker
@InternalSdkApi
@Serializable
data class ActionableData<A: ActionModel>(
    val actions: List<A>? = emptyList<A>(),
    val defaultTapAction: BasicActionModel? = null,
)
