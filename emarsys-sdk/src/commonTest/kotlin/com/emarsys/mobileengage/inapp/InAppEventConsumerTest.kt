package com.emarsys.mobileengage.inapp

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.inapp.InAppPresentationMode.Overlay
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class InAppEventConsumerTest {
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockInAppPresenter: InAppPresenterApi
    private lateinit var mockInAppViewProvider: InAppViewProviderApi
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkEventFlow = MutableSharedFlow<SdkEvent>(
            replay = 100,
            extraBufferCapacity = Channel.UNLIMITED
        )
        mockSdkEventManager = mock(MockMode.autofill)
        everySuspend { mockSdkEventManager.sdkEventFlow } returns sdkEventFlow
        mockSdkLogger = mock(MockMode.autofill)
        mockInAppPresenter = mock(MockMode.autofill)
        mockInAppViewProvider = mock(MockMode.autofill)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createInAppEventHandler(applicationScope : CoroutineScope) : InAppEventConsumer {
        return InAppEventConsumer(
            applicationScope = applicationScope,
            sdkEventManager = mockSdkEventManager,
            sdkLogger = mockSdkLogger,
            inAppPresenter = mockInAppPresenter,
            inAppViewProvider = mockInAppViewProvider
        )
    }

    @Test
    fun testRegister_should_start_consume_on_channel() = runTest {
        val inAppEventHandler = createInAppEventHandler(backgroundScope)
        inAppEventHandler.register()

        verifySuspend { mockSdkEventManager.sdkEventFlow.filter(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testEventConsumer_shouldCallPresentWithCorrectParameters() = runTest {
        createInAppEventHandler(backgroundScope).register()
        val mockInAppView: InAppViewApi = mock(MockMode.autofill)
        val mockWebViewHolder: WebViewHolder = mock(MockMode.autofill)
        val inAppMessage = InAppMessage(
            dismissId = "testDismissId",
            trackingInfo = "{}",
            content = "testContent"
        )
        val event = SdkEvent.Internal.InApp.Present(
            inAppMessage = inAppMessage
        )

        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppView.load(inAppMessage) } returns mockWebViewHolder

        val sdkEvents = backgroundScope.async {
            sdkEventFlow.take(1).toList()
        }

        sdkEventFlow.emit(event)

        sdkEvents.await() shouldBe listOf(event)
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppView.load(inAppMessage) }
        verifySuspend { mockInAppPresenter.present(mockInAppView, mockWebViewHolder,Overlay) }
    }

}