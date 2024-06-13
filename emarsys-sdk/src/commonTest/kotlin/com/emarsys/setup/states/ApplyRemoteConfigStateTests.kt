package com.emarsys.setup.states

import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ApplyRemoteConfigStateTests {
    private lateinit var mockRemoteConfigHandler: RemoteConfigHandlerApi
    private lateinit var applyRemoteConfigState: ApplyRemoteConfigState

    @BeforeTest
    fun setUp() {
        mockRemoteConfigHandler = mock()
        applyRemoteConfigState = ApplyRemoteConfigState(mockRemoteConfigHandler)
    }

    @Test
    fun testName() = runTest {
        applyRemoteConfigState.name shouldBe "applyRemoteConfig"
    }

    @Test
    fun testActive() = runTest {
        everySuspend { mockRemoteConfigHandler.handle() } returns Unit

        applyRemoteConfigState.active()

        verifySuspend { mockRemoteConfigHandler.handle() }
    }
}