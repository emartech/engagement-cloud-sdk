package com.emarsys.core.crypto

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CryptoTests {
    private companion object {
        const val PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    private val crypto = Crypto(PUBLIC_KEY)

    @Test
    fun verify_should_return_true() = runTest {
        val testMessage = "testData"
        val signature = "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="

        val result = crypto.verify(testMessage, signature)

        result shouldBe true
    }

    @Test
    fun verify_should_return_false() = runTest {
        val testMessage = "testData2"
        val signature = "MEUCIQDb6AxUK2W4IyKJ/P02Y0BNlm2ioP7ytu3dOyumc4hN8gIgEzwKmeCtd6Jn9Neg4Epn+oSkV4wAJNmfAgeeAM0u7Nw="

        val result = crypto.verify(testMessage, signature)

        result shouldBe false
    }
}