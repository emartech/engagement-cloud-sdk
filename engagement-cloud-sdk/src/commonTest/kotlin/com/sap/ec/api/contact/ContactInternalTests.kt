package com.sap.ec.api.contact

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactInternalTests {
    private companion object {
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val linkContact = ContactCall.LinkContact(CONTACT_FIELD_VALUE)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(OPEN_ID_TOKEN)
        val unlinkContact = ContactCall.UnlinkContact()
        val calls = mutableListOf(linkContact, linkAuthenticatedContact, unlinkContact)
    }

    private lateinit var contactContext: ContactContextApi
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var contactInternal: ContactInstance

    @BeforeTest
    fun setUp() {
        eventSlot = slot()
        contactContext = ContactContext(calls)
        sdkEventDistributor = mock(MockMode.autofill)
        everySuspend { sdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        contactInternal = ContactInternal(
            contactContext,
            sdkLogger = mock(MockMode.autofill),
            sdkEventDistributor
        )
    }

    @Test
    fun testLinkContact_should_emit_linkContact_event_into_sdkFlow() = runTest {
        contactInternal.link(CONTACT_FIELD_VALUE)

        val emitted = eventSlot.get()
        (emitted is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
        (emitted as SdkEvent.Internal.Sdk.LinkContact).contactFieldValue shouldBe CONTACT_FIELD_VALUE
    }

    @Test
    fun testLinkAuthenticatedContact_should_emit_linkAuthenticatedContact_event_into_sdkFlow() =
        runTest {
            contactInternal.linkAuthenticated(OPEN_ID_TOKEN)

            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.LinkAuthenticatedContact) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.LinkAuthenticatedContact).openIdToken shouldBe OPEN_ID_TOKEN
        }

    @Test
    fun testUnlinkContact_should_emit_unlinkContact_event_into_sdkFlow() = runTest {
        contactInternal.unlink()

        val emitted = eventSlot.get()
        (emitted is SdkEvent.Internal.Sdk.UnlinkContact) shouldBe true
    }

    @Test
    fun testActivate_should_emit_stored_calls_as_events_to_event_flow() = runTest {
        contactInternal.activate()

        verifySuspend(VerifyMode.exactly(3)) { sdkEventDistributor.registerEvent(any()) }

        contactContext.calls.size shouldBe 0
    }
}