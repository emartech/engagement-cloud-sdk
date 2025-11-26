package com.emarsys.core.url

import com.emarsys.TestEmarsysConfig
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.MissingApplicationCodeException
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UrlFactoryTests {
    private companion object {
        const val APPLICATION_CODE = "testAppCode"
        const val CLIENT_SERVICE_BASE_URL = "https://me-client.gservice.emarsys.net"
        val config = TestEmarsysConfig(APPLICATION_CODE)
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var urlFactory: UrlFactoryApi

    @BeforeTest
    fun setUp() {
        mockDefaultUrls = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns CLIENT_SERVICE_BASE_URL
        every { mockSdkContext.config } returns config
        urlFactory = UrlFactory(mockSdkContext)
    }

    @Test
    fun testCreate_shouldTrowException_whenApplicationCode_isNull() {
        forAll(
            table(
                headers("urls"),
                listOf(
                    row(EmarsysUrlType.REFRESH_TOKEN),
                    row(EmarsysUrlType.CHANGE_APPLICATION_CODE),
                    row(EmarsysUrlType.LINK_CONTACT),
                    row(EmarsysUrlType.UNLINK_CONTACT),
                    row(EmarsysUrlType.REFRESH_TOKEN),
                    row(EmarsysUrlType.PUSH_TOKEN),
                    row(EmarsysUrlType.REGISTER_DEVICE_INFO),
                    row(EmarsysUrlType.EVENT),
                    row(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE),
                    row(EmarsysUrlType.REMOTE_CONFIG),
                )
            )
        ) {
            val testUrl = "https://me-client.gservice.emarsys.net"
            every { mockSdkContext.defaultUrls } returns mockDefaultUrls
            every { mockDefaultUrls.clientServiceBaseUrl } returns testUrl
            every { mockDefaultUrls.eventServiceBaseUrl } returns testUrl
            every { mockDefaultUrls.remoteConfigBaseUrl } returns testUrl
            val config = TestEmarsysConfig()
            every { mockSdkContext.config } returns config

            shouldThrow<MissingApplicationCodeException> {
                urlFactory.create(it)
            }
        }
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact-token")
    }

    @Test
    fun testCreate_changeMerchantId_should_return_url_with_appCode() {

        val result = urlFactory.create(EmarsysUrlType.CHANGE_MERCHANT_ID)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact-token")
    }

    @Test
    fun testCreate_changeApplicationCode_should_return_url() {

        val result = urlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/app")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {

        val result = urlFactory.create(EmarsysUrlType.PUSH_TOKEN)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_clearPushToken_should_return_url() {
        val testEvent = SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = APPLICATION_CODE)

        val result = urlFactory.create(EmarsysUrlType.CLEAR_PUSH_TOKEN, testEvent)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_clearPushToken_should_throwException_ifEventIsNull() {

        shouldThrow<MissingApplicationCodeException> { urlFactory.create(EmarsysUrlType.CLEAR_PUSH_TOKEN) }
    }

    @Test
    fun testCreate_clearPushToken_should_throwException_ifAppCodeIsNull() {
        val testEvent = SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = null)

        shouldThrow<MissingApplicationCodeException> { urlFactory.create(EmarsysUrlType.CLEAR_PUSH_TOKEN, testEvent) }
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {

        val result = urlFactory.create(EmarsysUrlType.REGISTER_DEVICE_INFO)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() {
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() {
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG)

        result shouldBe Url("testRemoteConfigBaseUrl/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() {
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)

        result shouldBe Url("testRemoteConfigBaseUrl/signature/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_deepLink_should_return_url_for_trackDeepLink() {
        every { mockDefaultUrls.deepLinkBaseUrl } returns "testDeepLinkBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.DEEP_LINK)

        result shouldBe Url("testDeepLinkBaseUrl")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMessages() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.FETCH_EMBEDDED_MESSAGES)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/messages")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_badgeCount() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.FETCH_BADGE_COUNT)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/badge-count")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMeta() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.FETCH_META)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/meta")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_updateTagsForMessages() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(EmarsysUrlType.UPDATE_TAGS_FOR_MESSAGES)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/tags")
    }
}