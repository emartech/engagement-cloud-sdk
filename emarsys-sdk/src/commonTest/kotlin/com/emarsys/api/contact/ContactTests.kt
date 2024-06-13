package com.emarsys.api.contact

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        const val CONTACT_FIELD_ID = 42
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        val testException = Exception()
    }

    private lateinit var sdkContext: SdkContextApi
    private lateinit var mockLoggingContact: ContactInstance
    private lateinit var mockGathererContact: ContactInstance
    private lateinit var mockContactInternal: ContactInstance
    private lateinit var contact: Contact<ContactInstance, ContactInstance, ContactInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        mockLoggingContact = mock()
        mockGathererContact = mock()
        mockContactInternal = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspend { mockLoggingContact.activate() } returns Unit
        everySuspend { mockGathererContact.activate() } returns Unit
        everySuspend { mockContactInternal.activate() } returns Unit
        contact =
            Contact(mockLoggingContact, mockGathererContact, mockContactInternal, sdkContext)
        contact.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLinkContact_inactiveState() = runTest {
        everySuspend {
            mockLoggingContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifySuspend {
            mockLoggingContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkContact_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifySuspend {
            mockGathererContact.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkContact_activeState() = runTest {
        everySuspend {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifySuspend {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        }
    }

    @Test
    fun testLinkContact_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        val result = contact.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testLinkAuthenticatedContact_inactiveState() = runTest {
        everySuspend {
            mockLoggingContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifySuspend {
            mockLoggingContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifySuspend {
            mockGathererContact.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_activeState() = runTest {
        everySuspend {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifySuspend {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        }
    }

    @Test
    fun testLinkAuthenticatedContact_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.linkAuthenticatedContact(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        val result = contact.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testUnlinkContact_inactiveState() = runTest {
        everySuspend {
            mockLoggingContact.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.inactive)
        contact.unlinkContact()

        verifySuspend {
            mockLoggingContact.unlinkContact()
        }
    }

    @Test
    fun testUnlinkContact_onHoldState() = runTest {
        everySuspend {
            mockGathererContact.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        contact.unlinkContact()

        verifySuspend {
            mockGathererContact.unlinkContact()
        }
    }

    @Test
    fun test_unlinkContact_activeState() = runTest {
        everySuspend {
            mockContactInternal.unlinkContact()
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        contact.unlinkContact()

        verifySuspend {
            mockContactInternal.unlinkContact()
        }
    }

    @Test
    fun test_unlinkContact_activeState_throws() = runTest {
        everySuspend {
            mockContactInternal.unlinkContact()
        } throws testException

        sdkContext.setSdkState(SdkState.active)
        val result = contact.unlinkContact()

        result.exceptionOrNull() shouldBe testException
    }

}