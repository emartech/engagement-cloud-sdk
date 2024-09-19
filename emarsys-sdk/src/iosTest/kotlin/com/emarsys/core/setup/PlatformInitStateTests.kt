package com.emarsys.core.setup

import com.emarsys.mobileengage.push.IosPushInstance
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlatformInitStateTests {
    private lateinit var mockIosPush: IosPushInstance
    private lateinit var platformInitState: PlatformInitState

    @BeforeTest
    fun setup() = runTest {
        mockIosPush = mock()
        every { mockIosPush.registerEmarsysNotificationCenterDelegate() } returns Unit
        platformInitState = PlatformInitState(mockIosPush)
    }

    @Test
    fun `active should call registerEmarsysNotificationCenterDelegate on push`() = runTest{
        platformInitState.active()
        verify { mockIosPush.registerEmarsysNotificationCenterDelegate() }
    }
}