package com.emarsys.db_migration

import android.security.keystore.KeyProperties
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class SharedPreferenceCryptoTest {

    private lateinit var mockLogger: Logger

    private lateinit var keyStore: KeyStore


    @Before
    fun setup() {
        mockLogger = mockk(relaxed = true)
        keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.deleteEntry("emarsys_sdk_key_shared_pref_key_v3")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun init_shouldGenerateKey_ifNotPresent_inKeyStore() {
        mockkStatic(KeyGenerator::class)

        SharedPreferenceCrypto(mockLogger)

        verify { KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES) }
    }

    @Test
    fun init_shouldNotGenerateKey_ifPresent_inKeyStore() {
        mockkStatic(KeyGenerator::class)

        SharedPreferenceCrypto(mockLogger)
        SharedPreferenceCrypto(mockLogger)

        verify(exactly = 1) { KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES) }
    }

    @Test
    fun decrypt_shouldReturn_null_andGenerateNewSecretKey_ifGeneralSecurityExceptionHappens() =
        runTest {
            val testValue = "dGVzdFZhbHVlU2hvdWxkQmVTaXh0ZWVuQ2hhcnNMb25n"
            mockkStatic(KeyGenerator::class)
            mockkStatic(Cipher::class)
            every { Cipher.getInstance("AES/GCM/NoPadding") } throws GeneralSecurityException("Test exception")

            val testCrypto = SharedPreferenceCrypto(mockLogger)

            testCrypto.decrypt(testValue) shouldBe null

            verify { KeyGenerator.getInstance(any()) }
        }

    @Test
    fun decrypt_shouldReturn_encryptedValue_ifIllegalArgumentException_withBase64ErrorHappens() =
        runTest {
            val testValue = "testValueShouldBeSixteenCharsLong"

            val testCrypto = SharedPreferenceCrypto(mockLogger)
            testCrypto.decrypt(testValue) shouldBe testValue
        }

    @Test
    fun decrypt_shouldReturn_null_ifIllegalArgumentExceptionHappens() =
        runTest {
            val testValue = "dGVzdFZhbHVlU2hvdWxkQmVTaXh0ZWVuQ2hhcnNMb25n"
            mockkStatic(Cipher::class)
            every { Cipher.getInstance("AES/GCM/NoPadding") } throws IllegalArgumentException("Test exception")

            val testCrypto = SharedPreferenceCrypto(mockLogger)
            testCrypto.decrypt(testValue) shouldBe null
        }

    @Test
    fun decrypt_shouldReturn_null_ifExceptionHappens() =
        runTest {
            val testValue = "testValue"

            val testCrypto = SharedPreferenceCrypto(mockLogger)
            testCrypto.decrypt(testValue) shouldBe null
        }
}