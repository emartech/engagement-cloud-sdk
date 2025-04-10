package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.ActionablePush
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.DisplayablePush
import com.emarsys.mobileengage.push.PushMessage
import kotlinx.serialization.Serializable

@Serializable
data class JsPushMessage(
    override val trackingInfo: String,
    override val platformData: JsPlatformData,
    override val badgeCount: BadgeCount?,
    override val actionableData: ActionableData<PresentableActionModel>?,
    override val displayableData: DisplayableData?
): PushMessage<JsPlatformData>, ActionablePush<PresentableActionModel>, DisplayablePush