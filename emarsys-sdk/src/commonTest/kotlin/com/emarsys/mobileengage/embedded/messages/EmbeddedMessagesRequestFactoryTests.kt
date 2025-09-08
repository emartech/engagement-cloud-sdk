package com.emarsys.mobileengage.embedded.messages


import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmbeddedMessagesRequestFactoryTests {

    private lateinit var embeddedMessagesRequestFactory: EmbeddedMessagesRequestFactory
    private lateinit var mockUrlFactory: UrlFactoryApi

    @BeforeTest
    fun setup() {
        mockUrlFactory = mock(MockMode.autofill)

        every { mockUrlFactory.create(EmarsysUrlType.FETCH_EMBEDDED_MESSAGES) } returns Url("https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages")

        embeddedMessagesRequestFactory = EmbeddedMessagesRequestFactory(mockUrlFactory)
    }

    @Test
    fun create_fetchMessages_should_return_request_for_fetchMessages_endpoint() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 0,
                categoryIds = emptyList()
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages"
    }

    @Test
    fun create_fetchMessages_should_return_request_for_fetchMessages_endpoint_whenOffset_isSmallerThanZero() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = -12,
                categoryIds = emptyList()
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages"
    }

    @Test
    fun create_fetchMessages_should_return_request_for_fetchMessages_withOffsetAndCategoryIds() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 40,
                categoryIds = listOf("category1", "category2")
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?skip=40&categoryIds=category1%2Ccategory2"
    }

}