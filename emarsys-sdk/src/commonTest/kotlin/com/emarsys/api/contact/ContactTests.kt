package com.emarsys.api.contact

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import io.kotest.common.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    companion object {
        val contactFieldId = 42
        val contactFieldValue = "testContactFieldValue"
        val openIdToken = "testOpenIdToken"
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
    fun test_linkContact_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingContact.linkContact(
                contactFieldId,
                contactFieldValue
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(contactFieldId, contactFieldValue)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.linkContact(
                contactFieldId,
                contactFieldValue
            )
        }
    }

    @Test
    fun test_linkContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererContact.linkContact(
                contactFieldId,
                contactFieldValue
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(contactFieldId, contactFieldValue)

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.linkContact(
                contactFieldId,
                contactFieldValue
            )
        }
    }

    @Test
    fun test_linkContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockContactInternal.linkContact(
                contactFieldId,
                contactFieldValue
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkContact(contactFieldId, contactFieldValue)

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.linkContact(
                contactFieldId,
                contactFieldValue
            )
        }
    }

    @Test
    fun test_linkAuthenticatedContact_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingContact.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        }
    }

    @Test
    fun test_linkAuthenticatedContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererContact.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        }
    }

    @Test
    fun test_linkAuthenticatedContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockContactInternal.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        } returns Unit

        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, mockSdkContext)

        contact.linkAuthenticatedContact(contactFieldId, openIdToken)

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.linkAuthenticatedContact(
                contactFieldId,
                openIdToken
            )
        }
    }

    @Test
    fun test_unlinkContact_inactiveState() = runTest {
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
    fun test_unlinkContact_onHoldState() = runTest {
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