package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.BUTTON_CLICK_ORIGIN
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test


class ReportingActionTests {
    private companion object {
        const val ID = "testId"
        const val TRACKING_INFO = """{"key:"value"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val BUTTON_ORIGIN = "button"
        const val TEST_URL = "testUrl"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setUp() = runTest {
        mockSdkEventDistributor = mock()
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_pushButtonClicked() = runTest {
        val pushButtonClickedActionModel = BasicPushButtonClickedActionModel(ID, TRACKING_INFO)
        val action = ReportingAction(pushButtonClickedActionModel, mockSdkEventDistributor)
        val expectedEvent = SdkEvent.Internal.Push.Clicked(
            ID,
            reporting = REPORTING,
            trackingInfo = TRACKING_INFO,
            attributes = buildJsonObject {
                put(
                    "origin", JsonPrimitive(BUTTON_ORIGIN)
                )
            }
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked() = runTest {
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(
            REPORTING,
            TRACKING_INFO
        )
        val action = ReportingAction(inAppButtonClickedActionModel, mockSdkEventDistributor)
        val expectedEvent = SdkEvent.Internal.InApp.ButtonClicked(
            ID,
            reporting = REPORTING,
            trackingInfo = TRACKING_INFO,
            attributes = buildJsonObject {
                put("origin", JsonPrimitive(BUTTON_CLICK_ORIGIN))
            }
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
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
                attributes = buildJsonObject {
                    put(
                        "origin", JsonPrimitive("main")
                    )
                }
            )

            val eventSlot = slot<SdkEvent>()

            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns
                    mock(MockMode.autofill)

            action.invoke()

            verifyArguments(eventSlot, expectedEvent)
        }

    private fun verifyArguments(
        eventSlot: SlotCapture<SdkEvent>,
        expectedEvent: SdkEvent
    ) {
        eventSlot.get().name shouldBe expectedEvent.name
        eventSlot.get().attributes shouldBe expectedEvent.attributes
    }
}