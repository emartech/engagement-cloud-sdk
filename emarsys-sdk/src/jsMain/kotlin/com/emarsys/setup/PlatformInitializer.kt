package com.emarsys.setup

import com.emarsys.api.push.PushConstants.WEB_PUSH_SDK_READY_CHANNEL_NAME
import com.emarsys.mobileengage.push.PushNotificationClickHandlerApi
import org.w3c.dom.BroadcastChannel

class PlatformInitializer(
    private val pushNotificationClickHandler: PushNotificationClickHandlerApi
) :
    PlatformInitializerApi {

    private val readyBroadcastChannel = BroadcastChannel(WEB_PUSH_SDK_READY_CHANNEL_NAME)

    override suspend fun init() {
        pushNotificationClickHandler.register()
        readyBroadcastChannel.postMessage("READY")
    }

}