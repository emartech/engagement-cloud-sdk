package com.emarsys.mobileengage.push

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushGatherer
import com.emarsys.core.storage.StringStorageApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

class IosGathererPush(
    private val context: ApiContext<PushCall>,
    storage: StringStorageApi,
    private val iosPushInternal: IosPushInstance
) : PushGatherer(context, storage), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() = iosPushInternal.customerUserNotificationCenterDelegate
        set(value) {
            iosPushInternal.customerUserNotificationCenterDelegate = value
        }
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = iosPushInternal.emarsysUserNotificationCenterDelegate

    override suspend fun handleSilentMessageWithUserInfo(userInfo: BasicPushUserInfo) {
        context.calls.add(PushCall.HandleMessageWithUserInfo(userInfo))
    }
}