package com.emarsys.mobileengage.push

import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.SilentPushUserInfo
import com.emarsys.core.storage.StringStorageApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

internal class IosGathererPush(
    private val context: PushContextApi,
    storage: StringStorageApi,
    private val iosPushInternal: IosPushInstance
) : PushGatherer(context, storage), IosPushInstance {
    override var customerUserNotificationCenterDelegate: List<UNUserNotificationCenterDelegateProtocol>
        get() = iosPushInternal.customerUserNotificationCenterDelegate
        set(value) {
            iosPushInternal.customerUserNotificationCenterDelegate = value
        }
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = iosPushInternal.emarsysUserNotificationCenterDelegate

    override suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo) {
        context.calls.add(PushCall.HandleSilentMessageWithUserInfo(userInfo))
    }
}