package com.emarsys.reregistration.states

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.session.SessionContext
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test

class LinkContactStateTests {

    companion object {
        private const val TEST_CONTACT_FIELD_ID = 4
        private const val TEST_CONTACT_FIELD_VALUE = "testContactFieldValue"
        private const val TEST_OPEN_ID_TOKEN = "testOpenIdToken"
    }

    private lateinit var sessionContext: SessionContext
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var linkContactState: LinkContactState

    @BeforeTest
    fun setUp() {
        eventSlot = slot()
        sessionContext = SessionContext()
        mockSdkContext = mock()
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )

        linkContactState =
            LinkContactState(
                sessionContext,
                mockSdkContext,
                mockSdkEventDistributor,
                sdkLogger = mock(MockMode.autofill)
            )
    }

    @Test
    fun active_shouldRegisterLinkContactEvent_throughSdkEventDistributor_whenContactFieldIdAndValue_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldId } returns TEST_CONTACT_FIELD_ID
            sessionContext.contactFieldValue = TEST_CONTACT_FIELD_VALUE

            linkContactState.active()

            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
            registeredEvent.attributes?.get("contactFieldId")?.jsonPrimitive?.content?.toInt() shouldBe TEST_CONTACT_FIELD_ID
            registeredEvent.attributes?.get("contactFieldValue")?.jsonPrimitive?.content shouldBe TEST_CONTACT_FIELD_VALUE
        }

    @Test
    fun active_shouldRegisterLinkAuthenticatedContactEvent_throughSdkEventDistributor_whenContactFieldIdAndOpenIdToken_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldId } returns TEST_CONTACT_FIELD_ID
            sessionContext.contactFieldValue = null
            sessionContext.openIdToken = TEST_OPEN_ID_TOKEN

            linkContactState.active()

            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkAuthenticatedContact) shouldBe true
            registeredEvent.attributes?.get("contactFieldId")?.jsonPrimitive?.content?.toInt() shouldBe TEST_CONTACT_FIELD_ID
            registeredEvent.attributes?.get("openIdToken")?.jsonPrimitive?.content shouldBe TEST_OPEN_ID_TOKEN
        }

    @Test
    fun active_shouldNotEmitEvent_whenNeitherContactFieldValue_norOpenIdToken_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldId } returns TEST_CONTACT_FIELD_ID
            sessionContext.contactFieldValue = null
            sessionContext.openIdToken = null

            linkContactState.active()

            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkEventDistributor.registerEvent(any())
            }
        }

    @Test
    fun active_RegisterLinkAuthenticatedContactEvent_throughSdkEventDistributor_evenWhenContactFieldId_isNotAvailable() =
        runTest {
            every { mockSdkContext.contactFieldId } returns null
            sessionContext.contactFieldValue = TEST_CONTACT_FIELD_VALUE
            sessionContext.openIdToken = null

            linkContactState.active()

            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
            registeredEvent.attributes?.get("contactFieldId") shouldBe JsonNull
            registeredEvent.attributes?.get("contactFieldValue")?.jsonPrimitive?.content shouldBe TEST_CONTACT_FIELD_VALUE
        }

}