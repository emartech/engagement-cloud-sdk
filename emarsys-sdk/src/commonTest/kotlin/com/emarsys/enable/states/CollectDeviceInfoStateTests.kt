package com.emarsys.enable.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.context.RequestContext
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CollectDeviceInfoStateTests {
    private var requestContext: RequestContext = RequestContext()
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var collectDeviceInfoState: CollectDeviceInfoState

    @BeforeTest
    fun setUp() {
        mockDeviceInfoCollector = mock()
        collectDeviceInfoState = CollectDeviceInfoState(mockDeviceInfoCollector, requestContext)
    }

    @AfterTest
    fun tearDown() {
        requestContext = RequestContext()
    }

    @Test
    fun activate_should_collect_deviceInfo_and_set_sessionContext() = runTest {
        val clientId = "testClientId"
        everySuspend { mockDeviceInfoCollector.getClientId() } returns clientId

        collectDeviceInfoState.active()

        verifySuspend { mockDeviceInfoCollector.getClientId() }
        requestContext.clientId shouldBe clientId
    }
}