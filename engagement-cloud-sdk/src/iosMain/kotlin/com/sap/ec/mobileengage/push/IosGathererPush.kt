package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.PushCall
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.api.push.PushGatherer
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.storage.StringStorageApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal class IosGathererPush(
    private val context: PushContextApi,
    storage: StringStorageApi,
    private val iosPushInternal: IosPushInstance,
    sdkContext: SdkContextApi
) : PushGatherer(context, storage, sdkContext), IosPushInstance {
    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
        get() = iosPushInternal.customerUserNotificationCenterDelegate
        set(value) {
            iosPushInternal.customerUserNotificationCenterDelegate = value
        }
    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = iosPushInternal.userNotificationCenterDelegate

    override suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo) {
        context.calls.add(PushCall.HandleSilentMessageWithUserInfo(userInfo))
    }
}