package com.emarsys.setup.states

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
        clients = listOf(
            mockDeviceClient,
            mockConfigClient,
            mockDeeplinkClient,
            mockContactClient,
            mockEventClient,
            mockPushClient
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
        }
    }
}