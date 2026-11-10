package com.sap.ec.core.url

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.ServiceUrls
import com.sap.ec.core.exceptions.SdkException.ServiceUrlsUnavailableException
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UrlFactoryTests {
    private companion object {
        const val APPLICATION_CODE = "testAppCode"
        const val REMOTE_CONFIG_BASE_URL = "testRemoteConfigBaseUrl"
        const val CLIENT_SERVICE_BASE_URL = "testClientServiceBaseUrl"
        const val EVENT_SERVICE_BASE_URL = "testEventServiceBaseUrl"
        const val DEEP_LINK_BASE_URL = "testDeepLinkBaseUrl"
        const val EMBEDDED_MESSAGING_BASE_URL = "testEmbeddedMessagingBaseUrl"
        const val LOGGING_URL = "testLoggingUrl"
        val config = TestEngagementCloudSDKConfig(APPLICATION_CODE)
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var urlFactory: UrlFactoryApi

    @BeforeTest
    fun setUp() {
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.serviceUrls } returns ServiceUrls(
            remoteConfigBaseUrl = REMOTE_CONFIG_BASE_URL,
            globalServiceUrls = ServiceUrls.Global(
                deepLinkBaseUrl = DEEP_LINK_BASE_URL,
                jsBridgeUrl = "",
                jsBridgeSignatureUrl = ""
            ),
            appCodeBasedServiceUrls = ServiceUrls.AppCodeBased(
                clientServiceBaseUrl = CLIENT_SERVICE_BASE_URL,
                eventServiceBaseUrl = EVENT_SERVICE_BASE_URL,
                embeddedMessagingBaseUrl = EMBEDDED_MESSAGING_BASE_URL,
                loggingUrl = LOGGING_URL
            )
        )
        everySuspend { mockSdkContext.getSdkConfig() } returns config
        urlFactory = UrlFactory(mockSdkContext)
    }

    @Test
    fun testCreate_refreshTokenUrl_should_return_url_with_appCode() = runTest {
        val result = urlFactory.create(ECUrlType.RefreshToken)

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/contact-token")
    }

    @Test
    fun testCreate_changeApplicationCode_should_return_url() = runTest {

        val result = urlFactory.create(ECUrlType.ChangeApplicationCode)

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/app")
    }

    @Test
    fun testCreate_registerPushToken_should_return_url() = runTest {

        val result = urlFactory.create(ECUrlType.PushToken)

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_clearPushToken_should_return_url() = runTest {
        val result = urlFactory.create(ECUrlType.ClearPushToken(applicationCode = APPLICATION_CODE))

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/push-token")
    }

    @Test
    fun testCreate_registerDeviceInfo_should_return_url() = runTest {

        val result = urlFactory.create(ECUrlType.RegisterDeviceInfo)

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client")
    }

    @Test
    fun testCreate_linkContact_should_return_url_withAppCode() = runTest {
        val result = urlFactory.create(ECUrlType.LinkContact)

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/contact")
    }

    @Test
    fun testCreate_unlinkContact_should_return_url_withAppCode() = runTest {
        val result = urlFactory.create(ECUrlType.UnlinkContact(applicationCode = APPLICATION_CODE))

        result shouldBe Url("$CLIENT_SERVICE_BASE_URL/v4/apps/$APPLICATION_CODE/client/contact")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfig() = runTest {
        val result = urlFactory.create(ECUrlType.RemoteConfig)

        result shouldBe Url("$REMOTE_CONFIG_BASE_URL/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_remoteConfig_should_return_url_for_remoteConfigSignature() = runTest {

        val result = urlFactory.create(ECUrlType.RemoteConfigSignature)

        result shouldBe Url("$REMOTE_CONFIG_BASE_URL/signature/$APPLICATION_CODE")
    }

    @Test
    fun testCreate_globalRemoteConfig_should_return_url_for_globalRemoteConfig() = runTest {
        val result = urlFactory.create(ECUrlType.GlobalRemoteConfig)

        result shouldBe Url("$REMOTE_CONFIG_BASE_URL/GLOBAL")
    }

    @Test
    fun testCreate_globalRemoteConfigSignature_should_return_url_for_globalRemoteConfigSignature() = runTest {
        val result = urlFactory.create(ECUrlType.GlobalRemoteConfigSignature)

        result shouldBe Url("$REMOTE_CONFIG_BASE_URL/signature/GLOBAL")
    }

    @Test
    fun testCreate_deepLink_should_return_url_for_trackDeepLink() = runTest {
        val result = urlFactory.create(ECUrlType.DeepLink)

        result shouldBe Url(DEEP_LINK_BASE_URL)
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMessages() = runTest {
        val result = urlFactory.create(ECUrlType.FetchEmbeddedMessages)

        result shouldBe Url("$EMBEDDED_MESSAGING_BASE_URL/embedded-messaging/api/v1/$APPLICATION_CODE/messages")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_badgeCount() = runTest {
        val result = urlFactory.create(ECUrlType.FetchBadgeCount)

        result shouldBe Url("$EMBEDDED_MESSAGING_BASE_URL/embedded-messaging/api/v1/$APPLICATION_CODE/badge-count")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_fetchMeta() = runTest {
        val result = urlFactory.create(ECUrlType.FetchMeta)

        result shouldBe Url("$EMBEDDED_MESSAGING_BASE_URL/embedded-messaging/api/v1/$APPLICATION_CODE/meta")
    }

    @Test
    fun testCreate_embeddedMessaging_should_return_url_for_updateTagsForMessages() = runTest {
        val result = urlFactory.create(ECUrlType.UpdateTagsForMessages)

        result shouldBe Url("$EMBEDDED_MESSAGING_BASE_URL/embedded-messaging/api/v1/$APPLICATION_CODE/tags")
    }

    @Test
    fun testCreate_inlineInAppMessages_should_return_url() = runTest {
        val result = urlFactory.create(ECUrlType.FetchInlineInAppMessages)

        result shouldBe Url("$EVENT_SERVICE_BASE_URL/v5/apps/$APPLICATION_CODE/inline-messages")
    }

    @Test
    fun testCreate_event_should_return_url_for_event() = runTest {
        val result = urlFactory.create(ECUrlType.Event)

        result shouldBe Url("$EVENT_SERVICE_BASE_URL/v5/apps/$APPLICATION_CODE/client/events")
    }

    @Test
    fun testCreate_logging_should_return_url_for_logging() = runTest {
        val result = urlFactory.create(ECUrlType.Logging)

        result shouldBe Url("$LOGGING_URL/v1/log")
    }

    @Test
    fun testCreate_shouldThrowServiceUrlsUnavailableException_whenGlobalServiceUrlsNull() = runTest {
        every { mockSdkContext.serviceUrls } returns ServiceUrls(remoteConfigBaseUrl = "")

        shouldThrow<ServiceUrlsUnavailableException> { urlFactory.create(ECUrlType.DeepLink) }
    }

    @Test
    fun testCreate_shouldThrowServiceUrlsUnavailableException_whenAppCodeBasedServiceUrlsNull() = runTest {
        every { mockSdkContext.serviceUrls } returns ServiceUrls(remoteConfigBaseUrl = "")

        shouldThrow<ServiceUrlsUnavailableException> { urlFactory.create(ECUrlType.Logging) }
    }
}