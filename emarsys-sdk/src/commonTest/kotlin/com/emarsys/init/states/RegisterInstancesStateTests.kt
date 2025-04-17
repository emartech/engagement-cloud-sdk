package com.emarsys.init.states

import com.emarsys.api.contact.ContactApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
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

    @BeforeTest
    fun setup() {
        mockEventTrackerApi = mock()
        mockContactApi = mock()
        mockPushApi = mock()
        registerInstancesState = RegisterInstancesState(
            mockEventTrackerApi,
            mockContactApi,
            mockPushApi,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun testName() = runTest {
        registerInstancesState.name shouldBe "registerInstanceState"
    }

    @Test
    fun testActive_should_call_registerOnContext_on_instances() = runTest {
        everySuspend { mockEventTrackerApi.registerOnContext() } returns Unit
        everySuspend { mockContactApi.registerOnContext() } returns Unit
        everySuspend { mockPushApi.registerOnContext() } returns Unit

        registerInstancesState.active()

        verifySuspend { mockEventTrackerApi.registerOnContext() }
        verifySuspend { mockContactApi.registerOnContext() }
        verifySuspend { mockPushApi.registerOnContext() }
    }
}