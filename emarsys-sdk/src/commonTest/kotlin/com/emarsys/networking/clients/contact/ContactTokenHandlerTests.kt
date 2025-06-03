package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactTokenHandlerTests {

    private lateinit var requestContext: RequestContext
    private lateinit var contactTokenHandler: ContactTokenHandlerApi
    private val json: Json = JsonUtil.json

    @BeforeTest
    fun setUp() {
        requestContext = RequestContext()
        contactTokenHandler = ContactTokenHandler(requestContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testHandleContactTokens_should_set_tokens_to_sessionContext() = runTest {
        val refreshToken = "testRefreshToken"
        val contactToken = "testContactToken"
        val response = Response(
            UrlRequest(Url(""), HttpMethod.Post, null, null),
            HttpStatusCode.OK,
            headersOf(),
            json.encodeToString(ContactTokenResponseBody(refreshToken, contactToken))
        )

        contactTokenHandler.handleContactTokens(response)

        requestContext.refreshToken shouldBe refreshToken
        requestContext.contactToken shouldBe contactToken
    }

    @Test
    fun testHandleContactTokens_should_not_crash_when_response_does_not_contain_tokens() = runTest {
        val response = Response(
            UrlRequest(Url(""), HttpMethod.Post, null, null),
            HttpStatusCode.OK,
            headersOf(),
            "{}"
        )

        contactTokenHandler.handleContactTokens(response)
    }
}