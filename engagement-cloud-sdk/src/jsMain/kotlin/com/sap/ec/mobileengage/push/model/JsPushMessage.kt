package com.sap.ec.mobileengage.push.model

import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.ActionablePush
import com.sap.ec.mobileengage.push.DisplayableData
import com.sap.ec.mobileengage.push.DisplayablePush
import com.sap.ec.mobileengage.push.PushMessage
import kotlinx.serialization.Serializable

@Serializable
data class JsPushMessage(
    override val trackingInfo: String,
    override val platformData: JsPlatformData,
    override val badgeCount: BadgeCount?,
    override val actionableData: ActionableData<PresentableActionModel>?,
    override val displayableData: DisplayableData?
): PushMessage<JsPlatformData>, ActionablePush<PresentableActionModel>, DisplayablePush