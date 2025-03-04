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

class ApplyAppCodeBasedRemoteConfigStateTests {
    private lateinit var mockRemoteConfigHandler: RemoteConfigHandlerApi
    private lateinit var applyAppCodeBasedRemoteConfigState: ApplyAppCodeBasedRemoteConfigState

    @BeforeTest
    fun setUp() {
        mockRemoteConfigHandler = mock()
        applyAppCodeBasedRemoteConfigState = ApplyAppCodeBasedRemoteConfigState(mockRemoteConfigHandler)
    }

    @Test
    fun testName() = runTest {
        applyAppCodeBasedRemoteConfigState.name shouldBe "applyRemoteConfig"
    }

    @Test
    fun testActive() = runTest {
        everySuspend { mockRemoteConfigHandler.handleAppCodeBased() } returns Unit

        applyAppCodeBasedRemoteConfigState.active()

        verifySuspend { mockRemoteConfigHandler.handleAppCodeBased() }
    }
}