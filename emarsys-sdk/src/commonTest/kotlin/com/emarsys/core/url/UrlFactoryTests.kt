package com.emarsys.core.url

import com.emarsys.EmarsysConfig
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContextApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlin.test.BeforeTest

import kotlin.test.Test

class UrlFactoryTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var urlFactory: UrlFactoryApi

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock()
        mockDefaultUrls = mock()
        urlFactory = UrlFactory(mockSdkContext)
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/contact-token")
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_for_predict() {
        val config = EmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v4/contact-token")
    }

    @Test
    fun testCreate_changeApplicationCode_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/app")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.PUSH_TOKEN, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/push-token")
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_DEVICE_INFO, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/contact")
    }

    @Test
    fun testCreate_linkContact_should_return_url_for_predict() {
        val config = EmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v4/contact-token")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() {
        val config = EmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG, null)

        result shouldBe Url("testRemoteConfigBaseUrl/testAppCode")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() {
        val config = EmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE, null)

        result shouldBe Url("testRemoteConfigBaseUrl/signature/testAppCode")
    }

    @Test
    fun testCreate_deepLink_should_return_url_for_trackDeepLink() {
        val config = EmarsysConfig("testAppCode", null)
        every { mockDefaultUrls.deepLinkBaseUrl } returns "testDeepLinkBaseUrl"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.DEEP_LINK, null)

        result shouldBe Url("testDeepLinkBaseUrl")
    }
}