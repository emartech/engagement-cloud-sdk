package com.sap.ec.mobileengage.push.model

import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.ActionablePush
import com.sap.ec.mobileengage.push.DisplayableData
import com.sap.ec.mobileengage.push.DisplayablePush
import com.sap.ec.mobileengage.push.PushMessage
import kotlinx.serialization.Serializable

@Serializable
sealed class AndroidPush: PushMessage<AndroidPlatformData>

@Serializable
data class AndroidPushMessage(
    override val trackingInfo: String,
    override val platformData: AndroidPlatformData,
    override val badgeCount: BadgeCount? = null,
    override val displayableData: DisplayableData,
    override val actionableData: ActionableData<PresentableActionModel>? = null
): AndroidPush(), DisplayablePush, ActionablePush<PresentableActionModel>

@Serializable
data class SilentAndroidPushMessage(
    override val trackingInfo: String,
    override val platformData: AndroidPlatformData,
    override val badgeCount: BadgeCount?,
    override val actionableData: ActionableData<BasicActionModel>?
): AndroidPush(), ActionablePush<BasicActionModel>