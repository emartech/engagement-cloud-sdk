package com.sap.ec.init.states

import com.sap.ec.core.log.SdkLogger
import com.sap.ec.remoteConfig.RemoteConfig
import com.sap.ec.remoteConfig.RemoteConfigHandlerApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ApplySDKDefaultRemoteConfigStateTests {
    private lateinit var mockRemoteConfigHandler: RemoteConfigHandlerApi
    private lateinit var applySDKDefaultRemoteConfigState: ApplySDKDefaultRemoteConfigState

    @BeforeTest
    fun setup() {
        mockRemoteConfigHandler = mock(MockMode.autoUnit)

        applySDKDefaultRemoteConfigState = ApplySDKDefaultRemoteConfigState(
            mockRemoteConfigHandler,
            SdkLogger(
                "TestLoggerName",
                mock(MockMode.autofill),
                logConfigHolder = mock(MockMode.autofill)
            )
        )
    }

    @Test
    fun testName() = runTest {
        applySDKDefaultRemoteConfigState.name shouldBe "applySDKDefaultRemoteConfig"
    }

    @Test
    fun testActive_should_handleSDKDefault_with_remoteConfigHandler() = runTest {
        applySDKDefaultRemoteConfigState.active()

        verifySuspend { mockRemoteConfigHandler.handle(RemoteConfig.Default.SDK) }
    }
}