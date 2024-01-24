package com.emarsys.url

import com.emarsys.EmarsysConfig
import com.emarsys.context.DefaultUrls
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContextApi
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class UrlFactoryTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    @Mock
    lateinit var mockDefaultUrls: DefaultUrlsApi

    private var urlFactory: UrlFactoryApi by withMocks {
        UrlFactory(mockSdkContext)
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/apps/testAppCode/contact-token")
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_for_predict() {
        val config = EmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/contact-token")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_PUSH_TOKEN)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/apps/testAppCode/client/push-token")
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_DEVICE_INFO)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/apps/testAppCode/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/apps/testAppCode/client/contact")
    }

    @Test
    fun testCreate_linkContact_should_return_url_for_predict() {
        val config = EmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v3/contact")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() {
        val config = EmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG)

        result shouldBe Url("testRemoteConfigBaseUrl/testAppCode")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() {
        val config = EmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE)

        result shouldBe Url("testRemoteConfigBaseUrl/signature/testAppCode")
    }
}