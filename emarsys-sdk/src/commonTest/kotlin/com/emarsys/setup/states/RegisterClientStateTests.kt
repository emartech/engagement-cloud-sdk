package com.emarsys.setup.states

import com.emarsys.networking.clients.device.DeviceClientApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterClientStateTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockDeviceClient: DeviceClientApi

    private val registerClientState: RegisterClientState by withMocks {
        RegisterClientState(mockDeviceClient)
    }

    @BeforeTest
    fun setup() = runTest {
        everySuspending { mockDeviceClient.registerDeviceInfo() } returns Unit
    }

    @AfterTest
    fun tearDown() {
        mocker.reset()
    }

    @Test
    fun testActive_should_callDeviceClient() = runTest {
        registerClientState.active()

        verifyWithSuspend {
            mockDeviceClient.registerDeviceInfo()
        }
    }


}