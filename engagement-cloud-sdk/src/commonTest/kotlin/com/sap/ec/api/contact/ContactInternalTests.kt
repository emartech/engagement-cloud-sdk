package com.sap.ec.api.contact

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.OperationalEventDistributorApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.storage.StorageApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.matcher.capture.isAbsent
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactInternalTests {
    private companion object {
        const val STORE_ID = "testStoreId"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val APPLICATION_CODE = "testAppCode"
        const val CONTACT_HASH = "testContactHash"
        val linkContact = ContactCall.LinkContact(CONTACT_FIELD_VALUE)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(OPEN_ID_TOKEN)
        val unlinkContact = ContactCall.UnlinkContact(APPLICATION_CODE)
        val calls = mutableListOf(linkContact, linkAuthenticatedContact, unlinkContact)
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockOperationalEventDistributor: OperationalEventDistributorApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockCrypto: CryptoApi
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<ContactCall>
    private lateinit var mockStorage: StorageApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var contactInternal: ContactInstance

    @BeforeTest
    fun setUp() {
        eventSlot = slot()
        mockStorage = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(
            APPLICATION_CODE
        )
        mockRequestContext = mock(MockMode.autofill)
        mockCrypto = mock(MockMode.autofill)
        everySuspend { mockCrypto.hash(any()) } returns CONTACT_HASH
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        mockOperationalEventDistributor = mock(MockMode.autofill)
        everySuspend { mockOperationalEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        threadSafePersistentStore = createThreadSafeStore()
        contactInternal = createContactInternal()
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
    fun testUnlinkContact_should_emit_unlinkContact_event_into_sdkFlow_if_isContactLinked_is_true() =
        runTest {
            every { mockRequestContext.isContactLinked } returns true

            contactInternal.unlink()

            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.UnlinkContact) shouldBe true
        }

    @Test
    fun testUnlinkContact_should_not_emit_unlinkContact_event_into_sdkFlow_if_isContactLinked_is_false() =
        runTest {
            every { mockRequestContext.isContactLinked } returns false

            contactInternal.unlink()

            eventSlot.isAbsent shouldBe true
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
        }

    @Test
    fun testLinkContact_should_not_emit_event_when_same_contact_already_linked() = runTest {
        every { mockRequestContext.isContactLinked } returns true
        every { mockRequestContext.linkedContactHash } returns CONTACT_HASH

        contactInternal.link(CONTACT_FIELD_VALUE)

        eventSlot.isAbsent shouldBe true
        verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
    }

    @Test
    fun testLinkContact_should_emit_event_when_a_different_contact_is_linked() = runTest {
        every { mockRequestContext.isContactLinked } returns true
        every { mockRequestContext.linkedContactHash } returns "otherHash"

        contactInternal.link(CONTACT_FIELD_VALUE)

        (eventSlot.get() is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
    }

    @Test
    fun testLinkContact_should_emit_event_when_no_contact_is_linked() = runTest {
        every { mockRequestContext.isContactLinked } returns false
        every { mockRequestContext.linkedContactHash } returns CONTACT_HASH

        contactInternal.link(CONTACT_FIELD_VALUE)

        (eventSlot.get() is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
    }

    @Test
    fun testLinkAuthenticatedContact_should_not_emit_event_when_same_contact_already_linked() =
        runTest {
            every { mockRequestContext.isContactLinked } returns true
            every { mockRequestContext.linkedContactHash } returns CONTACT_HASH

            contactInternal.linkAuthenticated(OPEN_ID_TOKEN)

            eventSlot.isAbsent shouldBe true
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
        }

    @Test
    fun testActivate_should_emit_stored_calls_as_events_to_event_flow() = runTest {
        every { mockStorage.get(STORE_ID, any<KSerializer<List<Any>>>()) } returns calls
        val safeStore = createThreadSafeStore(mockStorage)
        val testInternal = createContactInternal(safeStore)

        testInternal.activate()

        verifySuspend(VerifyMode.exactly(2)) { mockSdkEventDistributor.registerEvent(any()) }
        verifySuspend(VerifyMode.exactly(1)) { mockOperationalEventDistributor.registerEvent(any()) }
    }

    private fun createContactInternal(persistentStore: ThreadSafePersistentStoreApi<ContactCall> = threadSafePersistentStore): ContactInternal =
        ContactInternal(
            mockSdkEventDistributor,
            mockOperationalEventDistributor,
            mockSdkContext,
            persistentStore,
            mockRequestContext,
            mockCrypto,
            sdkLogger = mock(MockMode.autofill)
        )

    private fun createThreadSafeStore(storage: StorageApi = mockStorage): ThreadSafePersistentStoreApi<ContactCall> {
        return ThreadSafePersistentStore(
            STORE_ID,
            storage,
            ContactCall.serializer()
        )
    }
}