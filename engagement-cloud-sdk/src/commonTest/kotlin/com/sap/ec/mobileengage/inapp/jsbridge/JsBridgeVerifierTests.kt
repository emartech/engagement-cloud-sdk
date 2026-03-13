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
    fun verifyJsBridge_shouldReturnSuccess_true_whenMd5MatchesCache() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns CACHED_MD5

        val result = verifier.verifyJsBridge()

        result shouldBe Result.success(Unit)
    }

    @Test
    fun verifyJsBridge_shouldReDownload_whenMd5DoesNotMatchCache() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns "differentMd5Hash"
        everySuspend { mockJsBridgeClient.fetchJSBridge() } returns Result.success(Unit)

        val result = verifier.verifyJsBridge()

        result shouldBe Result.success(Unit)
        verifySuspend { mockJsBridgeClient.fetchJSBridge() }
    }

    @Test
    fun verifyJsBridge_shouldReDownload_whenNoCachedMd5() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns null
        everySuspend { mockJsBridgeClient.fetchJSBridge() } returns Result.success(Unit)

        val result = verifier.verifyJsBridge()

        result shouldBe Result.success(Unit)
        verifySuspend { mockJsBridgeClient.fetchJSBridge() }
    }

    @Test
    fun verifyJsBridge_shouldReturnFailure_whenReDownloadFails() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns Result.success(CACHED_MD5)
        every { mockStringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY) } returns "differentMd5Hash"
        everySuspend { mockJsBridgeClient.fetchJSBridge() } returns
            Result.failure(IllegalStateException("verification failed"))

        val result = verifier.verifyJsBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun verifyJsBridge_shouldReturnFailure_whenFetchServerMd5Fails() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns
            Result.failure(Exception("network error"))

        val result = verifier.verifyJsBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun verifyJsBridge_shouldReturnFailure_whenNoGoogHashHeader() = runTest {
        everySuspend { mockJsBridgeClient.fetchServerMd5() } returns
            Result.failure(IllegalStateException("JsBridge HEAD response missing x-goog-hash MD5"))

        val result = verifier.verifyJsBridge()

        result.isFailure shouldBe true
    }
}
