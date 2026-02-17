package com.sap.ec.networking.clients.contact

import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verify
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContactTokenHandlerTests {

    private lateinit var requestContext: RequestContextApi
    private lateinit var contactTokenHandler: ContactTokenHandlerApi
    private val json: Json = JsonUtil.json

    @BeforeTest
    fun setUp() {
        requestContext = mock(MockMode.autofill)
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

        verify { requestContext.refreshToken = refreshToken }
        verify { requestContext.contactToken = contactToken }
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