package com.emarsys.enable.states

import com.emarsys.di.EventBasedClientTypes
import com.emarsys.networking.clients.EventBasedClientApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterEventBasedClientsStateTests {
    private lateinit var mockDeviceClient: EventBasedClientApi
    private lateinit var mockConfigClient: EventBasedClientApi
    private lateinit var mockDeeplinkClient: EventBasedClientApi
    private lateinit var mockContactClient: EventBasedClientApi
    private lateinit var mockEventClient: EventBasedClientApi
    private lateinit var mockPushClient: EventBasedClientApi
    private lateinit var mockRemoteConfigClient: EventBasedClientApi
    private lateinit var mockLoggingClient: EventBasedClientApi
    private lateinit var mockReregistrationClient: EventBasedClientApi
    private lateinit var mockEmbeddedMessagingClient: EventBasedClientApi
    private lateinit var clients: List<EventBasedClientApi>
    private lateinit var registerEventBasedClientsState: RegisterEventBasedClientsState

    @BeforeTest
    fun setUp() {
        mockDeviceClient = mock(MockMode.autoUnit)
        mockConfigClient = mock(MockMode.autoUnit)
        mockDeeplinkClient = mock(MockMode.autoUnit)
        mockContactClient = mock(MockMode.autoUnit)
        mockEventClient = mock(MockMode.autoUnit)
        mockPushClient = mock(MockMode.autoUnit)
        mockRemoteConfigClient = mock(MockMode.autoUnit)
        mockLoggingClient = mock(MockMode.autoUnit)
        mockReregistrationClient = mock(MockMode.autoUnit)
        mockEmbeddedMessagingClient = mock(MockMode.autoUnit)
        clients = listOf(
            mockDeviceClient,
            mockConfigClient,
            mockDeeplinkClient,
            mockContactClient,
            mockEventClient,
            mockPushClient,
            mockRemoteConfigClient,
            mockLoggingClient,
            mockReregistrationClient,
            mockEmbeddedMessagingClient
        )

        registerEventBasedClientsState = RegisterEventBasedClientsState(clients)
    }

    @Test
    fun testActive_should_registerAllClients() = runTest {
        registerEventBasedClientsState.active()

        clients.size shouldBe EventBasedClientTypes.entries.size

        verifySuspend {
            mockDeviceClient.register()
            mockConfigClient.register()
            mockDeeplinkClient.register()
            mockContactClient.register()
            mockEventClient.register()
            mockPushClient.register()
            mockRemoteConfigClient.register()
            mockLoggingClient.register()
            mockReregistrationClient.register()
            mockEmbeddedMessagingClient.register()
        }
    }
}