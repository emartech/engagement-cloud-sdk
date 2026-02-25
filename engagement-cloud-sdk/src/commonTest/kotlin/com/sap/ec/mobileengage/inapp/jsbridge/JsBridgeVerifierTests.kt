package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.networking.clients.jsbridge.JsBridgeClientApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class JsBridgeVerifierTests {
    private companion object {
        const val CACHED_MD5 = "rL0Y20zC+Fzt72VPzMSk2A=="
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockJsBridgeClient: JsBridgeClientApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var verifier: JsBridgeVerifier

    @BeforeTest
    fun setup() {
        mockStringStorage = mock(MockMode.autofill)
        mockJsBridgeClient = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        verifier = JsBridgeVerifier(
            stringStorage = mockStringStorage,
            jsBridgeClient = mockJsBridgeClient,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun shouldInject_shouldReturnSuccess_true_whenMd5MatchesCache() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns CACHED_MD5

        val result = verifier.shouldInjectJsBridge()

        result shouldBe Result.success(true)
    }

    @Test
    fun shouldInject_shouldReDownload_whenMd5DoesNotMatchCache() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns "differentMd5Hash"
        everySuspend { mockJsBridgeClient.validateJSBridge() } returns Result.success(Unit)

        val result = verifier.shouldInjectJsBridge()

        result shouldBe Result.success(true)
        verifySuspend { mockJsBridgeClient.validateJSBridge() }
    }

    @Test
    fun shouldInject_shouldReDownload_whenNoCachedMd5() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns null
        everySuspend { mockJsBridgeClient.validateJSBridge() } returns Result.success(Unit)

        val result = verifier.shouldInjectJsBridge()

        result shouldBe Result.success(true)
        verifySuspend { mockJsBridgeClient.validateJSBridge() }
    }

    @Test
    fun shouldInject_shouldReturnFailure_whenReDownloadFails() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns "differentMd5Hash"
        everySuspend { mockJsBridgeClient.validateJSBridge() } returns
            Result.failure(IllegalStateException("verification failed"))

        val result = verifier.shouldInjectJsBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun shouldInject_shouldReturnFailure_whenFetchServerMd5Fails() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns
            Result.failure(Exception("network error"))

        val result = verifier.shouldInjectJsBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun shouldInject_shouldReturnFailure_whenNoGoogHashHeader() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns
            Result.failure(IllegalStateException("JsBridge HEAD response missing x-goog-hash MD5"))

        val result = verifier.shouldInjectJsBridge()

        result.isFailure shouldBe true
    }
}
