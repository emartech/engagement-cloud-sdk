package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactTokenHandlerTests {

    private lateinit var sessionContext: SessionContext
    private lateinit var contactTokenHandler: ContactTokenHandlerApi
    private val json: Json = Json

    @BeforeTest
    fun setUp() {
        sessionContext = SessionContext()
        contactTokenHandler = ContactTokenHandler(sessionContext)
    }

    @Test
    fun testHandleContactTokens_should_set_tokens_to_sessionContext() {
        val refreshToken = "testRefreshToken"
        val contactToken = "testContactToken"
        val response = Response(
            UrlRequest(Url(""), HttpMethod.Post, null, null),
            HttpStatusCode.OK,
            headersOf(),
            json.encodeToString(ContactTokenResponseBody(refreshToken, contactToken))
        )

        contactTokenHandler.handleContactTokens(response)

        sessionContext.refreshToken shouldBe refreshToken
        sessionContext.contactToken shouldBe contactToken
    }

    @Test
    fun testHandleContactTokens_should_not_crash_when_response_does_not_contain_tokens() {
        val response = Response(
            UrlRequest(Url(""), HttpMethod.Post, null, null),
            HttpStatusCode.OK,
            headersOf(),
            "{}"
        )

        contactTokenHandler.handleContactTokens(response)
    }
}