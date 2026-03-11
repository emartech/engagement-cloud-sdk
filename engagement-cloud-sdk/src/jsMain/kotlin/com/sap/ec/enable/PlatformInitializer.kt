package com.sap.ec.enable

import com.sap.ec.api.push.PushConstants.WEB_PUSH_SDK_READY_CHANNEL_NAME
import com.sap.ec.core.badge.WebBadgeCountHandlerApi
import com.sap.ec.mobileengage.push.PushNotificationClickHandlerApi
import org.w3c.dom.BroadcastChannel

internal class PlatformInitializer(
    private val pushNotificationClickHandler: PushNotificationClickHandlerApi,
    private val webBadgeCountHandler: WebBadgeCountHandlerApi
) : PlatformInitializerApi {

    override suspend fun init() {
        pushNotificationClickHandler.register()
        webBadgeCountHandler.register()

        val readyBroadcastChannel = BroadcastChannel(WEB_PUSH_SDK_READY_CHANNEL_NAME)
        readyBroadcastChannel.postMessage("READY")
        readyBroadcastChannel.close()
    }

}