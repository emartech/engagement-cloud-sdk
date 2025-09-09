package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingClientTest {

    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager : SdkEventManagerApi

    private lateinit var applicationScope: CoroutineScope
    private lateinit var embeddedMessagingClient: EmbeddedMessagingClient

    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>

    private lateinit var mockEmbeddedMessagesRequestFactory: EmbeddedMessagingRequestFactoryApi

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)

        mockSdkEventManager = mock()

        mockEmbeddedMessagesRequestFactory = mock()



        onlineEvents = MutableSharedFlow()

        everySuspend {
            mockSdkLogger.debug(any<String>())
        }
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents

        embeddedMessagingClient = EmbeddedMessagingClient(
            sdkLogger = mockSdkLogger,
            sdkEventManager = mockSdkEventManager,
            applicationScope = backgroundScope,
            embeddedMessagingRequestFactory = mockEmbeddedMessagesRequestFactory
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createEmbeddedMessagingClient(applicationScope: CoroutineScope) =
        EmbeddedMessagingClient(
            sdkLogger = mockSdkLogger,
            sdkEventManager = mockSdkEventManager,
            applicationScope = applicationScope,
            embeddedMessagingRequestFactory = mockEmbeddedMessagesRequestFactory
        )

    @Test
    fun testRegister_should_log_event() = runTest {
        embeddedMessagingClient.register()

        verifySuspend(VerifyMode.Companion.exactly(1)) {
            mockSdkLogger.debug(any<String>())
        }
    }

    @Test
    fun testRegister_should_use_sdkManager_onlineSdkEvents() = runTest {

        embeddedMessagingClient.register()

        verifySuspend(VerifyMode.Companion.exactly(1)) {
            mockSdkEventManager.onlineSdkEvents
        }
    }

    @Test
    fun testRegister_should_get_FetchBadgeCountEvent_from_flow() = runTest {

        //GIVEN
        createEmbeddedMessagingClient(backgroundScope).register()
        val event = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)

        every {
            mockEmbeddedMessagesRequestFactory.create(
                event
            )
        } returns UrlRequest(
            url = Url("https://test.com"),
            method = HttpMethod.Get
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        //WHEN
        onlineEvents.emit(event)
        advanceUntilIdle()

        //THEN
        onlineSdkEvents.await() shouldNotBe null
        verifySuspend {
            mockEmbeddedMessagesRequestFactory.create(
                event
            )
        }


    }



}