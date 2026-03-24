package com.sap.ec.mobileengage.action.actions

import com.sap.ec.api.event.model.AppEvent
import com.sap.ec.api.event.model.EventSource
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AppEventActionTests {
    private companion object {
        const val TEST_NAME = "testName"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<AppEvent>
    private lateinit var completableDeferred: CompletableDeferred<Unit>

    @BeforeTest
    fun setup() {
        eventSlot = Capture.slot()
        mockSdkEventDistributor = mock(MockMode.autofill)
        completableDeferred = CompletableDeferred()
        everySuspend { mockSdkEventDistributor.registerPublicEvent(capture(eventSlot)) } calls {
            completableDeferred.complete(Unit)
        }
    }

    @Test
    fun invoke_shouldNotAddAttributes_toInvokedAction_ifActionModelPayload_isNull() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null)

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke()

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().payload shouldBe null
    }

    @Test
    fun invoke_shouldAddAttributes_toInvokedAction_ifActionModelPayload_isNotNull() = runTest {
        val testPayload = mapOf("key" to "value", "testKey" to "testValue")
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = testPayload)

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke()

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().payload shouldBe testPayload
    }

    @Test
    fun invoke_shouldSetSource_toInapp_whenSdkEventIsInApp() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null, source = EventSource.InApp)
        val inAppEvent = SdkEvent.Internal.InApp.ButtonClicked(
            reporting = "testReporting",
            trackingInfo = "testTrackingInfo",
            origin = "testOrigin"
        )

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke(inAppEvent)

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().source shouldBe EventSource.InApp
    }

    @Test
    fun invoke_shouldSetSource_toPush_whenSdkEventIsPush() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null, source = EventSource.Push)
        val pushEvent = SdkEvent.Internal.Push.Clicked(
            reporting = "testReporting",
            trackingInfo = "testTrackingInfo",
            origin = "testOrigin"
        )

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke(pushEvent)

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().source shouldBe EventSource.Push
    }

    @Test
    fun invoke_shouldSetSource_toOnEvent_whenSdkEventIsOnEventAction() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null, source = EventSource.OnEvent)
        val onEventAction = SdkEvent.Internal.OnEventAction(
            timestamp = kotlin.time.Clock.System.now()
        )

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke(onEventAction)

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().source shouldBe EventSource.OnEvent
    }

    @Test
    fun invoke_shouldSetSource_toNull_whenSdkEventIsNull() = runTest {
        val testActionModel = BasicAppEventActionModel(name = TEST_NAME, payload = null)

        AppEventAction(testActionModel, mockSdkEventDistributor).invoke(null)

        advanceUntilIdle()

        completableDeferred.await()
        eventSlot.get().source shouldBe null
    }
}
