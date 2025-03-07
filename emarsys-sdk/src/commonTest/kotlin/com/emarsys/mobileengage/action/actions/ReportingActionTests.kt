package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel

import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test


class ReportingActionTests {
    private companion object {
        const val ID = "testId"
        const val SID = "testSid"
        const val BUTTON_ORIGIN = "button"
        const val TEST_URL = "testUrl"
    }

    private lateinit var mockCustomEventChannel: MutableSharedFlow<SdkEvent>

    @BeforeTest
    fun setUp() = runTest {
        mockCustomEventChannel = mock()
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_pushButtonClicked() = runTest {
        val pushButtonClickedActionModel = BasicPushButtonClickedActionModel(ID, SID)
        val action = ReportingAction(pushButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = SdkEvent.Internal.Push.Clicked(
            attributes = buildJsonObject {
                put(
                    "buttonId", JsonPrimitive((ID))
                )
                put(
                    "sid", JsonPrimitive(SID)
                )
                put(
                    "origin", JsonPrimitive(BUTTON_ORIGIN)
                )
            }
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockCustomEventChannel.emit(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked() = runTest {
        val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, SID, TEST_URL)
        val action = ReportingAction(inAppButtonClickedActionModel, mockCustomEventChannel)
        val expectedEvent = SdkEvent.Internal.InApp.ButtonClicked(
            attributes = buildJsonObject {
                put(
                    "buttonId", JsonPrimitive((ID))
                )
                put(
                    "sid", JsonPrimitive(SID)
                )
                put(
                    "url", JsonPrimitive(TEST_URL)
                )
            }
        )

        val eventSlot = slot<SdkEvent>()

        everySuspend { mockCustomEventChannel.emit(capture(eventSlot)) } returns Unit

        action.invoke()

        verifyArguments(eventSlot, expectedEvent)
    }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_inAppButtonClicked_noSidAndUrl() =
        runTest {
            val inAppButtonClickedActionModel = BasicInAppButtonClickedActionModel(ID, SID)
            val action = ReportingAction(inAppButtonClickedActionModel, mockCustomEventChannel)
            val expectedEvent = SdkEvent.Internal.InApp.ButtonClicked(
                attributes = buildJsonObject {
                    put(
                        "buttonId", JsonPrimitive((ID))
                    )
                    put(
                        "sid", JsonPrimitive(SID)
                    )
                }
            )

            val eventSlot = slot<SdkEvent>()

            everySuspend { mockCustomEventChannel.emit(capture(eventSlot)) } returns Unit

            action.invoke()

            verifyArguments(eventSlot, expectedEvent)
        }

    @Test
    fun testInvoke_shouldSendEventWithProperPayload_whenActionModel_isNotificationOpenedActionModel() =
        runTest {
            val notificationOpenedActionModel = NotificationOpenedActionModel(SID)
            val action = ReportingAction(notificationOpenedActionModel, mockCustomEventChannel)
            val expectedEvent = SdkEvent.Internal.Push.Clicked(
                attributes = buildJsonObject {
                    put(
                        "sid", JsonPrimitive(SID)
                    )
                    put(
                        "origin", JsonPrimitive("main")
                    )
                }
            )

            val eventSlot = slot<SdkEvent>()

            everySuspend { mockCustomEventChannel.emit(capture(eventSlot)) } returns Unit

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