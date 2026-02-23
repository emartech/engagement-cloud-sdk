package com.sap.ec.core.url

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.MissingApplicationCodeException
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
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
        val config = TestEngagementCloudSDKConfig(APPLICATION_CODE)
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
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val result = urlFactory.create(ECUrlType.RefreshToken)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact-token")
    }

    @Test
    fun testCreate_changeMerchantId_should_return_url_with_appCode() {

        val result = urlFactory.create(ECUrlType.ChangeMerchantId)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact-token")
    }

    @Test
    fun testCreate_changeApplicationCode_should_return_url() {

        val result = urlFactory.create(ECUrlType.ChangeApplicationCode)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/app")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {

        val result = urlFactory.create(ECUrlType.PushToken)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_clearPushToken_should_return_url() {
        val testEvent = SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = APPLICATION_CODE)

        val result = urlFactory.create(ECUrlType.ClearPushToken, testEvent)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_clearPushToken_should_throwException_ifEventIsNull() {

        shouldThrow<MissingApplicationCodeException> { urlFactory.create(ECUrlType.ClearPushToken) }
    }

    @Test
    fun testCreate_clearPushToken_should_throwException_ifAppCodeIsNull() {
        val testEvent = SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = null)

        shouldThrow<MissingApplicationCodeException> { urlFactory.create(ECUrlType.ClearPushToken, testEvent) }
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {

        val result = urlFactory.create(ECUrlType.RegisterDeviceInfo)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() {
        val result = urlFactory.create(ECUrlType.LinkContact)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/$APPLICATION_CODE/client/contact")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() {
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"

        val result = urlFactory.create(ECUrlType.RemoteConfig)

        result shouldBe Url("testRemoteConfigBaseUrl/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() {
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"

        val result = urlFactory.create(ECUrlType.RemoteConfigSignature)

        result shouldBe Url("testRemoteConfigBaseUrl/signature/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_deepLink_should_return_url_for_trackDeepLink() {
        every { mockDefaultUrls.deepLinkBaseUrl } returns "testDeepLinkBaseUrl"

        val result = urlFactory.create(ECUrlType.DeepLink)

        result shouldBe Url("testDeepLinkBaseUrl")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMessages() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(ECUrlType.FetchEmbeddedMessages)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/messages")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_badgeCount() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(ECUrlType.FetchBadgeCount)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/badge-count")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMeta() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(ECUrlType.FetchMeta)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/meta")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_updateTagsForMessages() {
        every { mockDefaultUrls.embeddedMessagingBaseUrl } returns "testEmbeddedMessagingBaseUrl"

        val result = urlFactory.create(ECUrlType.UpdateTagsForMessages)

        result shouldBe Url("testEmbeddedMessagingBaseUrl/v1/$APPLICATION_CODE/tags")
    }

    @Test
    fun testCreate_inlineInAppMessages_should_return_url() {
        every { mockDefaultUrls.eventServiceBaseUrl } returns "testEventServiceBaseUrl"

        val result = urlFactory.create(ECUrlType.FetchInlineInAppMessages)
        result shouldBe Url("testEventServiceBaseUrl/v5/apps/$APPLICATION_CODE/inline-messages")
    }
}