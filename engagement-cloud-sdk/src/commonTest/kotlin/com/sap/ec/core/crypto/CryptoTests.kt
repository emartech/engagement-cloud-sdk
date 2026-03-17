package com.sap.ec.core.crypto

import com.sap.ec.core.exceptions.SdkException.DecryptionFailedException
import com.sap.ec.core.log.SdkLogger
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test

class CryptoTests {
    private companion object {
        const val PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
        const val TEST_SECRET = "test secret"
    }
    private val sdkLogger = SdkLogger("TestLoggerName", mock(MockMode.autofill), logConfigHolder = mock())
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

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testBase64Encoding_should_matchKtorFormat() {
        // Oracle: encode a known byte array and assert the exact Base64 string.
        // This test confirms Kotlin stdlib Base64.Default produces identical output
        // to Ktor's encodeBase64(), which was the previous implementation.
        val knownBytes = "Hello, World!".encodeToByteArray()
        val expected = "SGVsbG8sIFdvcmxkIQ=="

        val encoded = Base64.encode(knownBytes)
        encoded shouldBe expected

        val decoded = Base64.decode(encoded)
        decoded.decodeToString() shouldBe "Hello, World!"
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testBase64Decode_should_handleInputWithEmbeddedNewlines() {
        // Ktor's decodeBase64Bytes() was lenient with embedded newlines.
        // Kotlin stdlib Base64.Default is strict and rejects them.
        // This is safe because our usage in Crypto.kt only decodes:
        //   1. Public keys (constant strings without newlines)
        //   2. Signatures (single-line Base64 from backend)
        //   3. AES ciphertext produced by Base64.encode() (no newlines)
        // None of these sources contain embedded newlines.
        val withNewlines = "SGVsbG8s\nIFdvcmxkIQ=="
        val withoutNewlines = "SGVsbG8sIFdvcmxkIQ=="

        // Stdlib Base64.Default rejects newlines -- verify this strictness
        shouldThrow<IllegalArgumentException> {
            Base64.decode(withNewlines)
        }

        // The clean version decodes correctly
        val decoded = Base64.decode(withoutNewlines)
        decoded.decodeToString() shouldBe "Hello, World!"
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testBase64Decode_should_handleInputWithoutTrailingPadding() {
        // Ktor's decodeBase64Bytes() was lenient with missing padding (=).
        // Kotlin stdlib Base64.Default is strict and rejects unpadded input.
        // This is safe because our usage in Crypto.kt only decodes:
        //   1. Public keys (always properly padded standard Base64)
        //   2. Signatures (properly padded Base64 from backend)
        //   3. AES ciphertext produced by Base64.encode() (always padded)
        // None of these sources omit padding characters.
        val withPadding = "SGVsbG8sIFdvcmxkIQ=="
        val withoutPadding = "SGVsbG8sIFdvcmxkIQ"

        // Stdlib Base64.Default rejects missing padding -- verify this strictness
        shouldThrow<IllegalArgumentException> {
            Base64.decode(withoutPadding)
        }

        // The properly padded version decodes correctly
        val decoded = Base64.decode(withPadding)
        decoded.decodeToString() shouldBe "Hello, World!"
    }
}