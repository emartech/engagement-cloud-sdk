package com.emarsys.setup.states

import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ApplyRemoteConfigStateTests: TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockRemoteConfigHandler: RemoteConfigHandlerApi

    private var applyRemoteConfigState: ApplyRemoteConfigState by withMocks {
        ApplyRemoteConfigState(mockRemoteConfigHandler)
    }

    @Test
    fun testName() = runTest {
        applyRemoteConfigState.name shouldBe "applyRemoteConfig"
    }

    @Test
    fun testActive() = runTest {
        everySuspending { mockRemoteConfigHandler.handle() } returns Unit

        applyRemoteConfigState.active()

        verifyWithSuspend { mockRemoteConfigHandler.handle() }
    }
}