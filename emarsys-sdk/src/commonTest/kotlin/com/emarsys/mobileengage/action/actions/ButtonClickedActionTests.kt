package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
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


class ButtonClickedActionTests {
    private companion object {
        const val ID = "testId"
        const val SID = "testSid"
        const val PUSH_EVENT_NAME = "push:click"
        const val IN_APP_EVENT_NAME = "inapp:click"
        const val BUTTON_ORIGIN = "button"
        const val CAMPAIGN_ID = "testCampaignId"
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
        val action = ButtonClickedAction(pushButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            PUSH_EVENT_NAME,
            mapOf(
                "button_id" to ID,
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
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, CAMPAIGN_ID, SID, TEST_URL)
        val action = ButtonClickedAction(inAppButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            IN_APP_EVENT_NAME,
            mapOf(
                "buttonId" to ID,
                "sid" to SID,
                "campaignId" to CAMPAIGN_ID,
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
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, CAMPAIGN_ID)
        val action = ButtonClickedAction(inAppButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = Event(
            EventType.INTERNAL,
            IN_APP_EVENT_NAME,
            mapOf(
                "buttonId" to ID,
                "campaignId" to CAMPAIGN_ID,
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