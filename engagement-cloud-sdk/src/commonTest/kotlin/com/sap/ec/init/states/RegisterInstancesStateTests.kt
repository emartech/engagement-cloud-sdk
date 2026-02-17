package com.sap.ec.init.states

import com.sap.ec.api.config.ConfigApi
import com.sap.ec.api.contact.ContactApi
import com.sap.ec.api.embeddedmessaging.EmbeddedMessagingApi
import com.sap.ec.api.event.EventTrackerApi
import com.sap.ec.api.inapp.InAppApi
import com.sap.ec.api.push.PushApi
import com.sap.ec.core.log.SdkLogger
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterInstancesStateTests {
    private lateinit var registerInstancesState: RegisterInstancesState
    private lateinit var mockEventTrackerApi: EventTrackerApi
    private lateinit var mockContactApi: ContactApi
    private lateinit var mockPushApi: PushApi
    private lateinit var mockInAppApi: InAppApi
    private lateinit var mockEmbeddedMessagingApi: EmbeddedMessagingApi
    private lateinit var mockConfigApi: ConfigApi

    @BeforeTest
    fun setup() {
        mockEventTrackerApi = mock(MockMode.autofill)
        mockContactApi = mock(MockMode.autofill)
        mockPushApi = mock(MockMode.autofill)
        mockConfigApi = mock(MockMode.autofill)
        mockEmbeddedMessagingApi = mock(MockMode.autofill)
        mockInAppApi = mock(MockMode.autofill)
        registerInstancesState = RegisterInstancesState(
            mockEventTrackerApi,
            mockContactApi,
            mockConfigApi,
            mockPushApi,
            mockInAppApi,
            mockEmbeddedMessagingApi,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun testName() = runTest {
        registerInstancesState.name shouldBe "registerInstanceState"
    }

    @Test
    fun testActive_should_call_registerOnContext_on_instances() = runTest {

        registerInstancesState.active() shouldBe Result.success(Unit)

        verifySuspend { mockEventTrackerApi.registerOnContext() }
        verifySuspend { mockContactApi.registerOnContext() }
        verifySuspend { mockPushApi.registerOnContext() }
        verifySuspend { mockInAppApi.registerOnContext() }
        verifySuspend { mockConfigApi.registerOnContext() }
        verifySuspend { mockEmbeddedMessagingApi.registerOnContext() }
    }
}