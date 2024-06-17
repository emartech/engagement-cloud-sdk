package com.emarsys.setup.states

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.session.SessionContext
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CollectDeviceInfoStateTests {
    private var sessionContext: SessionContext = SessionContext()
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var collectDeviceInfoState: CollectDeviceInfoState

    @BeforeTest
    fun setUp() {
        mockDeviceInfoCollector = mock()
        collectDeviceInfoState = CollectDeviceInfoState(mockDeviceInfoCollector, sessionContext)
    }

    @AfterTest
    fun teardown() {
        sessionContext = SessionContext()
    }

    @Test
    fun activate_should_collect_deviceInfo_and_set_sessionContext() = runTest {
        val hwId = "testHardwareId"
        every { mockDeviceInfoCollector.getHardwareId() } returns hwId

        collectDeviceInfoState.active()

        verify { mockDeviceInfoCollector.getHardwareId() }
        sessionContext.clientId shouldBe hwId
    }
}