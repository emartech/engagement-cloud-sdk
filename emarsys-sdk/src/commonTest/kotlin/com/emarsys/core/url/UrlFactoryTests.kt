package com.emarsys.core.url

import com.emarsys.TestEmarsysConfig
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.MissingApplicationCodeException
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

class UrlFactoryTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var urlFactory: UrlFactoryApi

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock(MockMode.autofill)
        mockDefaultUrls = mock(MockMode.autofill)
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
            val config = TestEmarsysConfig(null, null)
            every { mockSdkContext.config } returns config

            shouldThrow<MissingApplicationCodeException> {
                urlFactory.create(it, null)
            }
        }
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val config = TestEmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/contact-token")
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_for_predict() {
        val config = TestEmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        every { mockSdkContext.isConfigPredictOnly() } returns true
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v4/contact-token")
    }

    @Test
    fun testCreate_changeApplicationCode_should_return_url() {
        val config = TestEmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/app")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {
        val config = TestEmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.PUSH_TOKEN, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/push-token")
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {
        val config = TestEmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_DEVICE_INFO, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() {
        val config = TestEmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.gservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)

        result shouldBe Url("https://me-client.gservice.emarsys.net/v4/apps/testAppCode/client/contact")
    }

    @Test
    fun testCreate_linkContact_should_return_url_for_predict() {
        val config = TestEmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        every { mockSdkContext.isConfigPredictOnly() } returns true
        val result = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)

        result shouldBe Url("https://me-client.eservice.emarsys.net/v4/contact-token")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() {
        val config = TestEmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG, null)

        result shouldBe Url("testRemoteConfigBaseUrl/testAppCode")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() {
        val config = TestEmarsysConfig("testAppCode", null)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.remoteConfigBaseUrl } returns "testRemoteConfigBaseUrl"
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE, null)

        result shouldBe Url("testRemoteConfigBaseUrl/signature/testAppCode")
    }

    @Test
    fun testCreate_deepLink_should_return_url_for_trackDeepLink() {
        val config = TestEmarsysConfig("testAppCode", null)
        every { mockDefaultUrls.deepLinkBaseUrl } returns "testDeepLinkBaseUrl"
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockSdkContext.config } returns config

        val result = urlFactory.create(EmarsysUrlType.DEEP_LINK, null)

        result shouldBe Url("testDeepLinkBaseUrl")
    }
}