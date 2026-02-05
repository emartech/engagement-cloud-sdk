package com.emarsys.db_migration

import android.content.SharedPreferences
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class LegacySharedPreferencesWrapperTest {

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockSharedPreferencesCrypto: SharedPreferenceCrypto

    private lateinit var legacySharedPreferencesWrapper: LegacySharedPreferencesWrapper

    @Before
    fun setUp() {
        mockSharedPreferences = mockk()
        mockSharedPreferencesCrypto = mockk()

        legacySharedPreferencesWrapper =
            LegacySharedPreferencesWrapper(mockSharedPreferences, mockSharedPreferencesCrypto)
    }

    @Test
    fun getDecryptedString_shouldReturnDecryptedValue_whenKeyIsFoundAndDecrypted() = runTest {
        val testKey = "testKey"
        val encryptedValue = "encryptedValue"
        val decryptedValue = "decryptedValue"
        every { mockSharedPreferences.getString(testKey, null) } returns encryptedValue
        coEvery { mockSharedPreferencesCrypto.decrypt(encryptedValue) } returns decryptedValue

        legacySharedPreferencesWrapper.getDecryptedString(testKey) shouldBe decryptedValue

        coVerify { mockSharedPreferences.getString(testKey, null) }
        coVerify { mockSharedPreferencesCrypto.decrypt(encryptedValue) }
    }

    @Test
    fun getDecryptedString_shouldReturnNull_whenKeyDoesNotExist() = runTest {
        val testKey = "non_existing_key"
        every { mockSharedPreferences.getString(testKey, null) } returns null

        legacySharedPreferencesWrapper.getDecryptedString(testKey) shouldBe null

        coVerify { mockSharedPreferences.getString(testKey, null) }
        coVerify(exactly = 0) { mockSharedPreferencesCrypto.decrypt(any()) }
    }

    @Test
    fun getDecryptedString_shouldReturnNull_whenDecryptReturnsNull() = runTest {
        val testKey = "testKey"
        val encryptedValue = "encryptedValue"
        every { mockSharedPreferences.getString(testKey, null) } returns encryptedValue
        coEvery { mockSharedPreferencesCrypto.decrypt(encryptedValue) } returns null

        legacySharedPreferencesWrapper.getDecryptedString(testKey) shouldBe null

        coVerify { mockSharedPreferences.getString(testKey, null) }
        coVerify { mockSharedPreferencesCrypto.decrypt(encryptedValue) }
    }
}