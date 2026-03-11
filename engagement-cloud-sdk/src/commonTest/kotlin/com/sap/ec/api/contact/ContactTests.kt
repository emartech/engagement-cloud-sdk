package com.sap.ec.api.contact

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactTests {
    private companion object {
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val testException = Exception()
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLoggingContact: ContactInstance
    private lateinit var mockGathererContact: ContactInstance
    private lateinit var mockContactInternal: ContactInstance
    private lateinit var contact: Contact<ContactInstance, ContactInstance, ContactInstance>


    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)

        mockLoggingContact = mock(MockMode.autofill)
        mockGathererContact = mock(MockMode.autofill)
        mockContactInternal = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns mainDispatcher

        contact =
            Contact(
                mockLoggingContact,
                mockGathererContact,
                mockContactInternal,
                mockSdkContext
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLink_unInitializedState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)
        contact.registerOnContext()

        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend { mockLoggingContact.link(CONTACT_FIELD_VALUE) }
    }

    @Test
    fun testLink_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        contact.registerOnContext()

        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend { mockGathererContact.link(CONTACT_FIELD_VALUE) }
    }

    @Test
    fun testLink_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        contact.registerOnContext()

        contact.link(CONTACT_FIELD_VALUE)

        verifySuspend { mockContactInternal.link(CONTACT_FIELD_VALUE) }
    }

    @Test
    fun testLink_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockContactInternal.link(CONTACT_FIELD_VALUE) } throws testException
        contact.registerOnContext()

        val result = contact.link(CONTACT_FIELD_VALUE)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testLinkAuthenticated_unInitializedState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)
        everySuspend {
            mockLoggingContact.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } returns Unit
        contact.registerOnContext()

        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend { mockLoggingContact.linkAuthenticated(OPEN_ID_TOKEN) }
    }

    @Test
    fun testLinkAuthenticated_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        contact.registerOnContext()

        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend { mockGathererContact.linkAuthenticated(OPEN_ID_TOKEN) }
    }

    @Test
    fun testLinkAuthenticated_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        contact.registerOnContext()

        contact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend { mockContactInternal.linkAuthenticated(OPEN_ID_TOKEN) }
    }

    @Test
    fun testLinkAuthenticated_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockContactInternal.linkAuthenticated(OPEN_ID_TOKEN) } throws testException
        contact.registerOnContext()

        val result = contact.linkAuthenticated(OPEN_ID_TOKEN)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testUnlink_UnInitializedState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)
        contact.registerOnContext()

        contact.unlink()

        verifySuspend { mockLoggingContact.unlink() }
    }

    @Test
    fun testUnlink_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        contact.registerOnContext()

        contact.unlink()

        verifySuspend { mockGathererContact.unlink() }
    }

    @Test
    fun test_unlink_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        contact.registerOnContext()

        contact.unlink()

        verifySuspend { mockContactInternal.unlink() }
    }

    @Test
    fun test_unlink_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockContactInternal.unlink() } throws testException
        contact.registerOnContext()

        val result = contact.unlink()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun link_shouldPropagateCancellationException_whenInternalApiThrowsCancellationException() =
        runTest {
            every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
            everySuspend { mockContactInternal.link(CONTACT_FIELD_VALUE) } throws CancellationException("test cancellation")
            contact.registerOnContext()

            shouldThrow<CancellationException> { contact.link(CONTACT_FIELD_VALUE) }
        }
}