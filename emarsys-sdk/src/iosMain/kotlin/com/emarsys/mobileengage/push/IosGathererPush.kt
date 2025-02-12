package com.emarsys.mobileengage.push

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushGatherer
import com.emarsys.core.storage.TypedStorageApi
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class IosGathererPush(context: ApiContext<PushCall>,
                      storage: TypedStorageApi<String?>
) : PushGatherer(context, storage), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() = null
        set(value) {}
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() = object: NSObject(), UNUserNotificationCenterDelegateProtocol {}

    override fun registerEmarsysNotificationCenterDelegate() {}

    override suspend fun handleSilentMessageWithUserInfo(userInfo: BasicPushUserInfo) {

    }
}