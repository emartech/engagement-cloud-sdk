package com.emarsys.api.contact

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.clients.contact.ContactClientApi
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ContactInternalTests : TestsWithMocks() {
    private companion object {
        const val CONTACT_FIELD_ID = 2575
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockContactClient: ContactClientApi

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
        ContactInternal(mockContactClient)
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
}