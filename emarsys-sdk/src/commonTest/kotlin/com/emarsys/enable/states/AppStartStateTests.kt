package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test

class AppStartStateTests {
    private companion object {
        val timestamp = Clock.System.now()
        const val UUID = "testUUID"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var appStartState: AppStartState

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock()
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        every { mockTimestampProvider.provide() } returns timestamp
        every { mockUuidProvider.provide() } returns UUID

        appStartState =
            AppStartState(mockSdkEventDistributor, mockTimestampProvider, mockUuidProvider)
    }

    @Test
    fun testActivate_should_send_appStartEvent_with_eventClient_when_it_was_not_completed_yet() =
        runTest {
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mock(MockMode.autofill)

            appStartState.active()

            everySuspend {
                mockSdkEventDistributor.registerEvent(expectedEvent)
            }
        }

    @Test
    fun testActivate_should_not_send_appStartEvent_with_eventClient_when_it_was_already_completed_yet() =
        runTest {
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mock(
                MockMode.autofill)

            appStartState.active()
            appStartState.active()

            everySuspend {
                repeat(1) {
                    mockSdkEventDistributor.registerEvent(expectedEvent)
                }
            }
        }
}