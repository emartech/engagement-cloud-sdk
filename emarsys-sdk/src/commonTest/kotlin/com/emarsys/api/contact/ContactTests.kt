package com.emarsys.api.contact

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var sdkContext: SdkContextApi

    private lateinit var contact: Contact<ContactInstance, ContactInstance, ContactInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspending { mockLoggingContact.activate() } returns Unit
        everySuspending { mockGathererContact.activate() } returns Unit
        everySuspending { mockContactInternal.activate() } returns Unit
        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, sdkContext)
        contact.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testLinkContact_inactiveState() = runTest {
        everySuspending {
            mockLoggingContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
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
        everySuspending {
            mockGathererContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
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
        everySuspending {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
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
        everySuspending {
            mockLoggingContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
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
        everySuspending {
            mockGathererContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
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
        everySuspending {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
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
        everySuspending {
            mockLoggingContact.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockLoggingContact.unlinkContact()
        }
    }

    @Test
    fun testUnlinkContact_onHoldState() = runTest {
        everySuspending {
            mockGathererContact.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockGathererContact.unlinkContact()
        }
    }

    @Test
    fun test_unlinkContact_activeState() = runTest {
        everySuspending {
            mockContactInternal.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        contact.unlinkContact()

        verifyWithSuspend(exhaustive = false) {
            mockContactInternal.unlinkContact()
        }
    }

}