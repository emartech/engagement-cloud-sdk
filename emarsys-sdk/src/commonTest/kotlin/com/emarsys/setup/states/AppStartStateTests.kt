package com.emarsys.setup.states

import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.EventClientApi

import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test

class AppStartStateTests {
    private companion object {
        val timestamp = Clock.System.now()
    }

    private lateinit var mockEventClient: EventClientApi
    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var appStartState: AppStartState

    @BeforeTest
    fun setUp() {
        mockEventClient = mock()
        mockTimestampProvider = mock()
        every { mockTimestampProvider.provide() } returns timestamp

        appStartState = AppStartState(mockEventClient, mockTimestampProvider)
    }

    @Test
    fun testActivate_should_send_appStartEvent_with_eventClient_when_it_was_not_completed_yet() =
        runTest {
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(timestamp = timestamp)
            everySuspend { mockEventClient.registerEvent(expectedEvent) } returns Unit

            appStartState.active()

            everySuspend {
                mockEventClient.registerEvent(expectedEvent)
            }
        }

    @Test
    fun testActivate_should_not_send_appStartEvent_with_eventClient_when_it_was_already_completed_yet() =
        runTest {
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(timestamp = timestamp)
            everySuspend { mockEventClient.registerEvent(expectedEvent) } returns Unit

            appStartState.active()
            appStartState.active()

            everySuspend {
                repeat(1) {
                    mockEventClient.registerEvent(expectedEvent)
                }
            }
        }
}