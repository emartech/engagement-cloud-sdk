package com.emarsys.setup

import com.emarsys.mobileengage.push.PushNotificationClickHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PlatformInitializerTests {

    @Test
    fun init_shouldRegisterPushNotificationClickHandler() = runTest {
        val pushNotificationClickHandler = mock<PushNotificationClickHandlerApi> {
            everySuspend { register() } returns Unit
        }
        val platformInitializer = PlatformInitializer(pushNotificationClickHandler)

        platformInitializer.init()

        verifySuspend { pushNotificationClickHandler.register() }
    }

}