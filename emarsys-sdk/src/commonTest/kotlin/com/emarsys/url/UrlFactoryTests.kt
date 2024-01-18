package com.emarsys.url

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.DefaultUrlsApi
import io.kotest.matchers.shouldBe
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class UrlFactoryTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    @Mock
    lateinit var mockDefaultUrls: DefaultUrlsApi

    private var urlFactory: FactoryApi<EmarsysUrlType, String> by withMocks {
        UrlFactory(mockSdkContext, mockDefaultUrls)
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

        result shouldBe "https://me-client.eservice.emarsys.net/v3/apps/testAppCode/contact-token"
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_for_predict() {
        val config = EmarsysConfig(null, "testMerchantId")
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

        result shouldBe "https://me-client.eservice.emarsys.net/v3/contact-token"
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_PUSH_TOKEN)

        result shouldBe "https://me-client.eservice.emarsys.net/v3/apps/testAppCode/client/push-token"
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() {
        val config = EmarsysConfig("testAppCode", null)
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns config
        val result = urlFactory.create(EmarsysUrlType.REGISTER_DEVICE_INFO)

        result shouldBe "https://me-client.eservice.emarsys.net/v3/apps/testAppCode/client"
    }
}