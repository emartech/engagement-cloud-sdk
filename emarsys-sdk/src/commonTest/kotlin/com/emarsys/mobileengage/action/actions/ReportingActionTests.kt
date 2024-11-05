package com.emarsys.mobileengage.action.actions

import com.emarsys.SdkConstants.PUSH_CLICKED_EVENT_NAME
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
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


class ReportingActionTests {
    private companion object {
        const val ID = "testId"
        const val SID = "testSid"
        const val PUSH_EVENT_NAME = "push:click"
        const val IN_APP_EVENT_NAME = "inapp:click"
        const val BUTTON_ORIGIN = "button"
        const val TEST_URL = "testUrl"
    }

    private lateinit var mockCustomEventChannel: CustomEventChannelApi

    @BeforeTest
    fun setUp() = runTest {
        mockCustomEventChannel = mock()
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_pushButtonClicked() = runTest {
        val pushButtonClickedActionModel = BasicPushButtonClickedActionModel(ID, SID)
        val action = ReportingAction(pushButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            PUSH_EVENT_NAME,
            mapOf(
                "buttonId" to ID,
                "sid" to SID,
                "origin" to BUTTON_ORIGIN
            )
        )

        val eventSlot = slot<Event>()

        everySuspend { mockCustomEventChannel.send(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked() = runTest {
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, SID, TEST_URL)
        val action = ReportingAction(inAppButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            IN_APP_EVENT_NAME,
            mapOf(
                "buttonId" to ID,
                "sid" to SID,
                "url" to TEST_URL
            )
        )

        val eventSlot = slot<Event>()

        everySuspend { mockCustomEventChannel.send(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked_noSidAndUrl() = runTest {
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, SID)
        val action = ReportingAction(inAppButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            IN_APP_EVENT_NAME,
            mapOf(
                "buttonId" to ID,
                "sid" to SID,
            )
        )

        val eventSlot = slot<Event>()

        everySuspend { mockCustomEventChannel.send(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_isNotificationOpenedActionModel() = runTest {
        val notificationOpenedActionModel = NotificationOpenedActionModel(SID)
        val action = ReportingAction(notificationOpenedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            PUSH_CLICKED_EVENT_NAME,
            mapOf(
                "sid" to SID,
                "origin" to "main"
            )
        )

        val eventSlot = slot<Event>()

        everySuspend { mockCustomEventChannel.send(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    private fun verifyArguments(
        eventSlot: SlotCapture<Event>,
        expectedEvent: Event
    ) {
        eventSlot.get().name shouldBe expectedEvent.name
        eventSlot.get().type shouldBe expectedEvent.type
        eventSlot.get().attributes shouldBe expectedEvent.attributes
    }
}