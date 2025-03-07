package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.ActionablePush
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.DisplayablePush
import com.emarsys.mobileengage.push.PushMessage
import kotlinx.serialization.Serializable

@Serializable
sealed class AndroidPush: PushMessage<AndroidPlatformData>

@Serializable
data class AndroidPushMessage(
    override val sid: String,
    override val campaignId: String,
    override val platformData: AndroidPlatformData,
    override val badgeCount: BadgeCount?,
    override val displayableData: DisplayableData,
    override val actionableData: ActionableData<PresentableActionModel>? = null
): AndroidPush(), DisplayablePush, ActionablePush<PresentableActionModel>

@Serializable
data class SilentAndroidPushMessage(
    override val sid: String,
    override val campaignId: String,
    override val platformData: AndroidPlatformData,
    override val badgeCount: BadgeCount?,
    override val actionableData: ActionableData<BasicActionModel>?
): AndroidPush(), ActionablePush<BasicActionModel>