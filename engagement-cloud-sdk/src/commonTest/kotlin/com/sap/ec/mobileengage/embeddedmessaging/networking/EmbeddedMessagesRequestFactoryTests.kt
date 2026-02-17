package com.sap.ec.mobileengage.embeddedmessaging.networking

import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.sap.ec.mobileengage.embeddedmessaging.models.TagOperation
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class EmbeddedMessagesRequestFactoryTests {

    private lateinit var embeddedMessagesRequestFactory: EmbeddedMessagesRequestFactory
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var json: Json

    @BeforeTest
    fun setup() {
        json = JsonUtil.json
        mockUrlFactory = mock(MockMode.autofill)

        every { mockUrlFactory.create(ECUrlType.FetchEmbeddedMessages) } returns Url("https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages")
        every { mockUrlFactory.create(ECUrlType.FetchBadgeCount) } returns Url("https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/badge-count")
        every { mockUrlFactory.create(ECUrlType.FetchMeta) } returns Url("https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/meta")
        every { mockUrlFactory.create(ECUrlType.UpdateTagsForMessages) } returns Url("https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/tags")

        embeddedMessagesRequestFactory = EmbeddedMessagesRequestFactory(mockUrlFactory, json)
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
    fun create_should_return_request_for_fetchMessages_endpoint_whenOffset_isSmallerThanZero() =
        runTest {
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
    fun create_should_return_request_for_fetchMessages_withOffsetAndCategoryIds() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 40,
                categoryIds = listOf(1, 2)
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=40&filterCategoryIds=1%2C2"
    }

    @Test
    fun create_should_return_request_for_fetchMessages_withFilterUnopenedTrue() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 40,
                categoryIds = listOf(1, 2),
                filterUnopenedMessages = true
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=40&filterCategoryIds=1%2C2&filterUnopened=true"
    }

    @Test
    fun create_should_return_request_for_fetchBadgeCount() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(nackCount = 0)
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/badge-count"
    }

    @Test
    fun create_should_return_request_for_fetchMeta() = runTest {
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchMeta(nackCount = 0)
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/meta"
    }

    @Test
    fun create_should_return_request_for_updateTagsForMessages() = runTest {
        val trackingInfo = """{"key1":"value1","key2":"value2"}"""
        val updateData = listOf(
            MessageTagUpdate(
                messageId = "messageId",
                operation = TagOperation.Add,
                tag = "seen",
                trackingInfo = trackingInfo
            )
        )

        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(
                nackCount = 0,
                updateData = updateData
            )
        )

        result.method shouldBe HttpMethod.Patch
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/tags"
        result.bodyString shouldBe JsonUtil.json.encodeToString(updateData)
    }

    @Test
    fun create_should_return_request_for_fetchNextPage() = runTest {
        val offset = 20
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                nackCount = 0,
                offset = offset,
                categoryIds = emptyList()
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=$offset"
    }

    @Test
    fun create_should_return_request_for_fetchNextPage_withNotEmptyCategoryIds() = runTest {
        val offset = 20
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                nackCount = 0,
                offset = offset,
                categoryIds = listOf(1, 2)
            )
        )

        result.method shouldBe HttpMethod.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=$offset&filterCategoryIds=1%2C2"
    }

    @Test
    fun create_should_return_request_for_fetchNextPage_withFilterUnopenedTrue() = runTest {
        val offset = 20
        val filterUnopenedMessages = true
        val result = embeddedMessagesRequestFactory.create(
            SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                nackCount = 0,
                offset = offset,
                categoryIds = emptyList(),
                filterUnopenedMessages = filterUnopenedMessages
            )
        )

        result.method shouldBe HttpMethod.Companion.Get
        result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=$offset&filterUnopened=$filterUnopenedMessages"
    }

    @Test
    fun create_should_return_request_for_fetchNextPage_withFilterUnopenedTrue_and_NotEmptyCategoryIds() =
        runTest {
            val offset = 20
            val filterUnopenedMessages = true
            val result = embeddedMessagesRequestFactory.create(
                SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                    nackCount = 0,
                    offset = offset,
                    categoryIds = listOf(1, 2),
                    filterUnopenedMessages = filterUnopenedMessages
                )
            )

            result.method shouldBe HttpMethod.Companion.Get
            result.url.toString() shouldBe "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api/v1/testAppCode/messages?%24skip=$offset&filterCategoryIds=1%2C2&filterUnopened=$filterUnopenedMessages"
        }

}