package com.emarsys.api.contact

import com.emarsys.api.generic.ApiContext
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.clients.contact.ContactClientApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ContactInternalTests  {
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

    private val contactInternal: ContactInstance by lazy {
        mockContactClient = mock()
        contactContext = ContactContext(calls)
        ContactInternal(mockContactClient, contactContext)
    }

    @Test
    fun testLinkContact_should_make_call_on_contactClient() = runTest {
        everySuspend {
            mockContactClient.linkContact(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE,
                null
            )
        } returns testResponse

        contactInternal.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE)

        verifySuspend {
            mockContactClient.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, null)
        }
    }

    @Test
    fun testLinkAuthenticatedContact_should_make_call_on_contactClient() = runTest {
        everySuspend {
            mockContactClient.linkContact(
                CONTACT_FIELD_ID,
                null,
                OPEN_ID_TOKEN
            )
        } returns testResponse

        contactInternal.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN)

        verifySuspend {
            mockContactClient.linkContact(CONTACT_FIELD_ID, null, OPEN_ID_TOKEN)
        }
    }

    @Test
    fun testUnlinkContact_should_make_call_on_contactClient() = runTest {
        everySuspend {
            mockContactClient.unlinkContact()
        } returns testResponse

        contactInternal.unlinkContact()

        verifySuspend { mockContactClient.unlinkContact() }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspend {
            mockContactClient.linkContact(linkContact.contactFieldId, linkContact.contactFieldValue)
        } returns testResponse

        everySuspend {
            mockContactClient.linkContact(
                linkAuthenticatedContact.contactFieldId,
                null,
                linkAuthenticatedContact.openIdToken
            )
        } returns testResponse

        everySuspend {
            mockContactClient.unlinkContact()
        } returns testResponse

        contactInternal.activate()

        verifySuspend {
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