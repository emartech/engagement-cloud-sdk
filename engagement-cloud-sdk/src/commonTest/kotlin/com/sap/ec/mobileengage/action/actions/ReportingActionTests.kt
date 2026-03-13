package com.sap.ec.mobileengage.action.actions

import com.sap.ec.KotlinPlatform
import com.sap.ec.SdkConstants
import com.sap.ec.SdkConstants.BUTTON_CLICK_ORIGIN
import com.sap.ec.SdkConstants.PUSH_CLICKED_EVENT_NAME
import com.sap.ec.SdkConstants.WEBPUSH_CLICKED_EVENT_NAME
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.currentPlatform
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class ReportingActionTests {
    private companion object {
        const val ID = "testId"
        const val TRACKING_INFO = """{"key:"value"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        val EXPECTED_PUSH_CLICK_NAME =
            if (currentPlatform == KotlinPlatform.JS) WEBPUSH_CLICKED_EVENT_NAME else PUSH_CLICKED_EVENT_NAME
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi

    @Test
    fun testInAppButtonClickedEventName_shouldUseNamespacedFormat() {
        SdkConstants.IN_APP_BUTTON_CLICKED_EVENT_NAME shouldBe "inapp:click"
    }

    @Test
    fun testWebPushClickedEventName_shouldUseNamespacedFormat() {
        SdkConstants.WEBPUSH_CLICKED_EVENT_NAME shouldBe "webpush:click"
    }

    @BeforeTest
    fun setUp() = runTest {
        mockSdkEventDistributor = mock()
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_pushButtonClicked() = runTest {
        val pushButtonClickedActionModel =
            BasicPushButtonClickedActionModel(reporting = REPORTING, TRACKING_INFO)
        val action = ReportingAction(pushButtonClickedActionModel, mockSdkEventDistributor)
        val expectedEvent = SdkEvent.Internal.Push.Clicked(
            ID,
            reporting = REPORTING,
            trackingInfo = TRACKING_INFO,
            origin = BUTTON_CLICK_ORIGIN
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )

        action.invoke()

        verifyArguments(eventSlot, expectedEvent, EXPECTED_PUSH_CLICK_NAME)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked() = runTest {
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(
            REPORTING,
            TRACKING_INFO,
            InAppType.INLINE
        )
        val action = ReportingAction(inAppButtonClickedActionModel, mockSdkEventDistributor)
        val expectedEvent = SdkEvent.Internal.InApp.ButtonClicked(
            id = ID,
            reporting = REPORTING,
            trackingInfo = TRACKING_INFO,
            origin = BUTTON_CLICK_ORIGIN,
            reportingName = "inapp:click"
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )

        action.invoke()

        verifyArguments(eventSlot, expectedEvent, "inapp:click")
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_EmbeddedMessagingButtonClicked() =
        runTest {
            val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(
                REPORTING,
                TRACKING_INFO,
                InAppType.EMBEDDED_MESSAGING
            )
            val action = ReportingAction(inAppButtonClickedActionModel, mockSdkEventDistributor)
            val expectedEvent = SdkEvent.Internal.InApp.ButtonClicked(
                id = ID,
                reporting = REPORTING,
                trackingInfo = TRACKING_INFO,
                origin = BUTTON_CLICK_ORIGIN,
                reportingName = "em:click"
            )

            val eventSlot = slot<SdkEvent>()

            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
                MockMode.autofill
            )

            action.invoke()

            verifyArguments(eventSlot, expectedEvent, "em:click")
        }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_isNotificationOpenedActionModel() =
        runTest {
            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
            val action = ReportingAction(notificationOpenedActionModel, mockSdkEventDistributor)
            val expectedEvent = SdkEvent.Internal.Push.Clicked(
                reporting = REPORTING,
                trackingInfo = TRACKING_INFO,
                origin = "main"
            )

            val eventSlot = slot<SdkEvent>()

            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns
                    mock(MockMode.autofill)

            action.invoke()

            verifyArguments(eventSlot, expectedEvent, EXPECTED_PUSH_CLICK_NAME)
        }

    private fun verifyArguments(
        eventSlot: SlotCapture<SdkEvent>,
        expectedEvent: SdkEvent.Internal.Reporting,
        expectedName: String
    ) {
        eventSlot.get().type shouldBe expectedEvent.type
        (eventSlot.get() as SdkEvent.Internal.Reporting).reporting shouldBe expectedEvent.reporting
        (eventSlot.get() as SdkEvent.Internal.Reporting).trackingInfo shouldBe expectedEvent.trackingInfo
        (eventSlot.get() as SdkEvent.DeviceEvent).name shouldBe expectedName
    }
}