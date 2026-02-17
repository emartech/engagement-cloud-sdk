package com.sap.ec.core.crypto

import com.sap.ec.core.exceptions.SdkException.DecryptionFailedException
import com.sap.ec.core.log.SdkLogger
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CryptoTests {
    private companion object {
        const val PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
        const val TEST_SECRET = "test secret"
    }
    private val sdkLogger = SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
    private val crypto = Crypto(sdkLogger,PUBLIC_KEY)

    @Test
    fun testVerify_should_returnTrue() = runTest {
        val testMessage = "testData"
        val signature =
            "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="

        val result = crypto.verify(testMessage, signature)

        result shouldBe true
    }

    @Test
    fun testVerify_should_returnFalse() = runTest {
        val testMessage = "testData2"
        val signature =
            "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="

        val result = crypto.verify(testMessage, signature)

        result shouldBe false
    }

    @Test
    fun testEncryptAndDecrypt() = runTest {
        val testValue = "test message"

        val encrypted = crypto.encrypt(testValue, TEST_SECRET)

        encrypted shouldNotBe testValue

        val decrypted = crypto.decrypt(encrypted, TEST_SECRET)

        decrypted shouldBe testValue
    }

    @Test
    fun testDecrypt_should_throw_anException_whenSecretIsWrong() = runTest {
        val testValue = "test message"

        val encrypted = crypto.encrypt(testValue, TEST_SECRET)

        encrypted shouldNotBe testValue
        shouldThrow<DecryptionFailedException> {
            crypto.decrypt(encrypted, TEST_SECRET + 2)
        }
    }
}