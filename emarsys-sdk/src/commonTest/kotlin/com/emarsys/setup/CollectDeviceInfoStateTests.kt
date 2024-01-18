package com.emarsys.setup

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.session.SessionContext
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.Test

class CollectDeviceInfoStateTests: TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    private var sessionContext: SessionContext = SessionContext()

    private val collectDeviceInfoState: CollectDeviceInfoState by withMocks {
        CollectDeviceInfoState(mockDeviceInfoCollector, sessionContext)
    }

    @AfterTest
    fun teardown() {
        sessionContext = SessionContext()
        mocker.reset()
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