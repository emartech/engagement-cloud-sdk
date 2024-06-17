package com.emarsys.setup.states

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.networking.clients.contact.ContactClientApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LinkAnonymousContactStateTests {
    private companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
    }

    private var sessionContext = SessionContext()
    private lateinit var mockContactClient: ContactClientApi
    private lateinit var linkAnonymousContactState: LinkAnonymousContactState

    @BeforeTest
    fun setUp() {
        mockContactClient = mock()
        linkAnonymousContactState = LinkAnonymousContactState(mockContactClient, sessionContext)
    }

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

    @AfterTest
    fun tearDown() {
        sessionContext = SessionContext()
        
    }

    @Test
    fun testActive_when_contactTokenIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.contactToken = CONTACT_TOKEN

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_openIdTokenIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.openIdToken = OPEN_ID_TOKEN

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_contactFieldValueIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.contactFieldValue = CONTACT_FIELD_VALUE

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_contactTokenAndOpenIdTokenIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.contactToken = CONTACT_TOKEN
        sessionContext.openIdToken = OPEN_ID_TOKEN

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_contactTokenAndContactFieldValueIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.contactToken = CONTACT_TOKEN
        sessionContext.contactFieldValue = CONTACT_FIELD_VALUE

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_openIdTokenAndContactFieldValueIsAvailable_should_not_callContactClient() = runTest {
        sessionContext.openIdToken = OPEN_ID_TOKEN
        sessionContext.contactFieldValue = CONTACT_FIELD_VALUE

        linkAnonymousContactState.active()

        verifySuspend {
            repeat(0) {
                mockContactClient.unlinkContact()
            }
        }
    }

    @Test
    fun testActive_when_noContactDataIsAvailable_should_callContactClient() = runTest {
        everySuspend { mockContactClient.unlinkContact() } returns testResponse

        linkAnonymousContactState.active()

        verifySuspend {
            mockContactClient.unlinkContact()
        }
    }

}