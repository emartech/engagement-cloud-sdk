package com.emarsys.setup

import com.emarsys.mobileengage.inapp.InappJsBridgeApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlatformInitStateTests {

    private lateinit var platformInitState: PlatformInitState
    private lateinit var mockJsBridge: InappJsBridgeApi

    @BeforeTest
    fun setup() = runTest {
        mockJsBridge = mock()
        everySuspend { mockJsBridge.register() } returns Unit
        platformInitState = PlatformInitState(mockJsBridge)
    }

    @Test
    fun activate_shouldCallRegister_onJsBridge_ifSdkContext_hasConfig() = runTest {
        platformInitState.active()

        verifySuspend { mockJsBridge.register() }
    }

    @Test
    fun activate_shouldNot_callRegister_onJsBridge_ifSdkContext_hasNoConfig() = runTest {
        platformInitState.active()

        verifySuspend {
            repeat(0) {
                mockJsBridge.register()
            }
        }
    }
}