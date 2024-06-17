package com.emarsys.setup.states

import com.emarsys.networking.clients.device.DeviceClientApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterClientStateTests {
    private lateinit var mockDeviceClient: DeviceClientApi
    private val registerClientState: RegisterClientState by lazy {
        RegisterClientState(mockDeviceClient)
    }

    @BeforeTest
    fun setup() = runTest {
        mockDeviceClient = mock()

        everySuspend { mockDeviceClient.registerDeviceInfo() } returns Unit
    }

    @Test
    fun testActive_should_callDeviceClient() = runTest {
        registerClientState.active()

        verifySuspend {
            mockDeviceClient.registerDeviceInfo()
        }
    }
}