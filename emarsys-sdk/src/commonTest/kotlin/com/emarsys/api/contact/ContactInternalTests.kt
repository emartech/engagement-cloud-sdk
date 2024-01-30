package com.emarsys.api.contact

import com.emarsys.api.generic.ApiContext
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.clients.contact.ContactClientApi
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ContactInternalTests : TestsWithMocks() {
    private companion object {
        const val CONTACT_FIELD_ID = 2575
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val contactFieldId = 42
        const val contactFieldValue = "testContactFieldValue"
        const val openIdToken = "testOpenIdToken"
        val linkContact = ContactCall.LinkContact(contactFieldId, contactFieldValue)
        val linkAuthenticatedContact =
            ContactCall.LinkAuthenticatedContact(contactFieldId, openIdToken)
        val unlinkContact = ContactCall.UnlinkContact()
        val calls = mutableListOf(linkContact, linkAuthenticatedContact, unlinkContact)
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockContactClient: ContactClientApi

    private lateinit var contactContext: ApiContext<ContactCall>

    private val testResponse = Response(
        UrlRequest(
            Url("https://www.emarsys.com"),
            HttpMethod.Post,
            ""
        ),
        HttpStatusCode.OK,
        headersOf(),
        ""
    )

    private val contactInternal: ContactInstance by withMocks {
        contactContext = ContactContext(calls)
        ContactInternal(mockContactClient, contactContext)
    }

    @Test
    fun testLinkContact_should_make_call_on_contactClient() = runTest {
        everySuspending {
            mockContactClient.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE,
                null
            )
        } returns testResponse

        contactInternal.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifyWithSuspend {
            mockContactClient.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, null)
        }
    }

    @Test
    fun testLinkAuthenticatedContact_should_make_call_on_contactClient() = runTest {
        everySuspending {
            mockContactClient.linkContact(
                CONTACT_FIELD_ID,
                null,
                OPEN_ID_TOKEN
            )
        } returns testResponse

        contactInternal.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifyWithSuspend {
            mockContactClient.linkContact(CONTACT_FIELD_ID, null, OPEN_ID_TOKEN)
        }
    }

    @Test
    fun testUnlinkContact_should_make_call_on_contactClient() = runTest {
        everySuspending {
            mockContactClient.unlinkContact()
        } returns testResponse

        contactInternal.unlinkContact()

        verifyWithSuspend { mockContactClient.unlinkContact() }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspending {
            mockContactClient.linkContact(linkContact.contactFieldId, linkContact.contactFieldValue)
        } returns testResponse

        everySuspending {
            mockContactClient.linkContact(
                linkAuthenticatedContact.contactFieldId,
                null,
                linkAuthenticatedContact.openIdToken
            )
        } returns testResponse

        everySuspending {
            mockContactClient.unlinkContact()
        } returns testResponse

        contactInternal.activate()

        verifyWithSuspend {
            mockContactClient.linkContact(
                linkContact.contactFieldId,
                linkContact.contactFieldValue
            )
            mockContactClient.linkContact(
                linkAuthenticatedContact.contactFieldId,
                null,
                linkAuthenticatedContact.openIdToken
            )
            mockContactClient.unlinkContact()
        }

        contactContext.calls.size shouldBe 0
    }
}