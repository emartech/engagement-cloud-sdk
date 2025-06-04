package com.emarsys.enable.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.context.RequestContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CollectDeviceInfoStateTests {
    private lateinit var requestContext: RequestContextApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var collectDeviceInfoState: CollectDeviceInfoState

    @BeforeTest
    fun setUp() {
        requestContext = mock(MockMode.autofill)
        mockDeviceInfoCollector = mock()
        collectDeviceInfoState = CollectDeviceInfoState(mockDeviceInfoCollector, requestContext)
    }

    @Test
    fun activate_should_collect_deviceInfo_and_set_sessionContext() = runTest {
        val clientId = "testClientId"
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        collectDeviceInfoState.active()

        verifySuspend { mockDeviceInfoCollector.getClientId() }
        verify { requestContext.clientId = clientId }
    }
}