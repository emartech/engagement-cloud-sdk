package com.emarsys.setup.states

import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.providers.Provider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class AppStartStateTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        val timestamp = Clock.System.now()
    }

    @Mock
    lateinit var mockEventClient: EventClientApi

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>

    private val appStartState: AppStartState by withMocks {
        every { mockTimestampProvider.provide() } returns timestamp
        AppStartState(mockEventClient, mockTimestampProvider)
    }

    @Test
    fun testActivate_should_send_appStartEvent_with_eventClient_when_it_was_not_completed_yet() =
        runTest {
            val expectedEvent = Event(
                EventType.INTERNAL,
                "app:start",
                null,
                timestamp.toString()
            )
            everySuspending { mockEventClient.registerEvent(expectedEvent) } returns Unit

            appStartState.active()

            everySuspending {
                mockEventClient.registerEvent(expectedEvent)
            }
        }

    @Test
    fun testActivate_should_not_send_appStartEvent_with_eventClient_when_it_was_already_completed_yet() =
        runTest {
            val expectedEvent = Event(
                EventType.INTERNAL,
                "app:start",
                null,
                timestamp.toString()
            )
            everySuspending { mockEventClient.registerEvent(expectedEvent) } returns Unit

            appStartState.active()
            appStartState.active()

            everySuspending {
                repeat(1) {
                    mockEventClient.registerEvent(expectedEvent)
                }
            }
        }
}