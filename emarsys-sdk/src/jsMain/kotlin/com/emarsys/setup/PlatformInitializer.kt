package com.emarsys.setup

import com.emarsys.mobileengage.push.PushNotificationClickHandlerApi

class PlatformInitializer(
    private val pushNotificationClickHandler: PushNotificationClickHandlerApi
) :
    PlatformInitializerApi {

    override suspend fun init() {
        pushNotificationClickHandler.register()
    }

}