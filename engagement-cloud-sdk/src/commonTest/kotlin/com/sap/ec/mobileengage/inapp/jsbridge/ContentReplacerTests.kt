package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.providers.sdkversion.SdkVersionProviderApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContentReplacerTests {
    private companion object {
        const val JS_BRIDGE_PLACEHOLDER = "<!-- EC-JS-BRIDGE-SCRIPT -->"
        const val SDK_VERSION_PLACEHOLDER = "EC-SDK-VERSION"
        const val TEST_SDK_VERSION = "9.8.7"
        const val TEST_INAPP_CONTENT = """<!doctype html>
<html>
<head>
  <meta sdkVersion="$SDK_VERSION_PLACEHOLDER">
  $JS_BRIDGE_PLACEHOLDER</head></html>"""
        const val TEST_JS_BRIDGE_HOST_URL = "https://example.com"
    }

    private lateinit var contentReplacer: ContentReplacerApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockSdkVersionProvider: SdkVersionProviderApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockJsBridgeVerifier: JsBridgeVerifierApi

    @BeforeTest
    fun setup() {
        mockDefaultUrls = mock(MockMode.autofill)
        every { mockDefaultUrls.jsBridgeUrl } returns TEST_JS_BRIDGE_HOST_URL
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        mockSdkVersionProvider = mock(MockMode.autofill)
        every { mockSdkVersionProvider.provide() } returns TEST_SDK_VERSION
        mockJsBridgeVerifier = mock(MockMode.autofill)
        everySuspend { mockJsBridgeVerifier.shouldInjectJsBridge() } returns Result.success(true)

        contentReplacer = ContentReplacer(mockSdkContext, mockSdkVersionProvider, mockJsBridgeVerifier)
    }

    @Test
    fun replace_shouldAdd_scriptSrcTag_toTheContent_whenInjectionAllowed() = runTest {
        everySuspend { mockJsBridgeVerifier.shouldInjectJsBridge() } returns Result.success(true)

        val result = contentReplacer.replace(TEST_INAPP_CONTENT)

        result.contains("<script src=\"$TEST_JS_BRIDGE_HOST_URL\"></script>") shouldBe true
        result.contains(JS_BRIDGE_PLACEHOLDER) shouldBe false
    }

    @Test
    fun replace_shouldRemovePlaceholder_whenInjectionDenied() = runTest {
        everySuspend { mockJsBridgeVerifier.shouldInjectJsBridge() } returns Result.success(false)

        val result = contentReplacer.replace(TEST_INAPP_CONTENT)

        result.contains(TEST_JS_BRIDGE_HOST_URL) shouldBe false
        result.contains(JS_BRIDGE_PLACEHOLDER) shouldBe false
    }

    @Test
    fun replace_shouldRemovePlaceholder_whenVerifierReturnsFailure() = runTest {
        everySuspend { mockJsBridgeVerifier.shouldInjectJsBridge() } returns
            Result.failure(Exception("error"))

        val result = contentReplacer.replace(TEST_INAPP_CONTENT)

        result.contains(TEST_JS_BRIDGE_HOST_URL) shouldBe false
        result.contains(JS_BRIDGE_PLACEHOLDER) shouldBe false
    }

    @Test
    fun replace_shouldAdd_theSdkVersion_toTheContent_byReplacing_thePlaceholder() = runTest {

        val result = contentReplacer.replace(TEST_INAPP_CONTENT)

        result.contains(TEST_SDK_VERSION) shouldBe true
        result.contains(SDK_VERSION_PLACEHOLDER) shouldBe false
    }

    @Test
    fun replace_shouldReturn_contentUnchanged_ifPlaceholdersAreMissing() = runTest {
        val testContent = "testContent"
        val result = contentReplacer.replace(testContent)

        result shouldBe testContent
    }
}
