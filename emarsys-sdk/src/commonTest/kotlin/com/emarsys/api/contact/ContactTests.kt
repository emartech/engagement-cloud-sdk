package com.emarsys.api.contact

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        const val CONTACT_FIELD_ID = 42
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
    }

    @Mock
    lateinit var mockLoggingContact: ContactInstance

    @Mock
    lateinit var mockGathererContact: ContactInstance

    @Mock
    lateinit var mockContactInternal: ContactInstance

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    lateinit var contact: Contact<ContactInstance, ContactInstance, ContactInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()

        everySuspending { mockLoggingContact.activate() } returns Unit
        everySuspending { mockGathererContact.activate() } returns Unit
        everySuspending { mockContactInternal.activate() } returns Unit
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testLinkContact_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testUnlinkContact_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingContact.unlinkContact()
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.unlinkContact()
        }
    }

    @Test
    fun testUnlinkContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererContact.unlinkContact()
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.unlinkContact()
        }
    }

    @Test
    fun test_unlinkContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockContactInternal.unlinkContact()
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.unlinkContact()
        }
    }

}