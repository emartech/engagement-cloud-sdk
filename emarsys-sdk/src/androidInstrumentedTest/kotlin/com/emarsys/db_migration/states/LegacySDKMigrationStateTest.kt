package com.emarsys.db_migration.states

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.database.sqlite.transaction
import com.emarsys.SdkConstants
import com.emarsys.SdkConstants.CLIENT_ID_STORAGE_KEY
import com.emarsys.api.push.PushConstants
import com.emarsys.applicationContext
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorage
import com.emarsys.db_migration.LegacyDBOpenHelper
import com.emarsys.db_migration.LegacySharedPreferencesWrapper
import com.emarsys.db_migration.SharedPreferenceCrypto
import com.emarsys.db_migration.SharedPreferenceCrypto.Companion.KEYSTORE_ALIAS
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class LegacySDKMigrationStateTest {


    private lateinit var mockSdkLogger: Logger
    private lateinit var stringStorageSpy: StringStorage
    private lateinit var requestContextSpy: RequestContextApi
    private lateinit var legacyDBOpenHelper: LegacyDBOpenHelper
    private lateinit var legacySharedPreferences: SharedPreferences

    private lateinit var legacySDKMigrationState: LegacySDKMigrationState

    private val secretKey = getOrCreateSecretKey()

    companion object Companion {
        const val TEST_CLIENT_ID = "test_client_id"
        const val TEST_CONTACT_TOKEN = "test_contact_token"
        const val TEST_REFRESH_TOKEN = "test_refresh_token"
        const val TEST_CLIENT_STATE = "test_client_state"
        const val TEST_PUSH_TOKEN = "test_push_token"
        const val TEST_DEVICE_EVENT_STATE = "test_device_event_state"
    }

    @Before
    fun setup() {
        mockSdkLogger = mockk(relaxed = true)
        legacySharedPreferences =
            applicationContext.getSharedPreferences(
                LegacySharedPreferencesWrapper.EMARSYS_SECURE_SHARED_PREFERENCES_V3_NAME,
                Context.MODE_PRIVATE
            )
        val sharedPreferencesCrypto = SharedPreferenceCrypto(sdkLogger = mockSdkLogger)
        val sharedPreferences = applicationContext.getSharedPreferences(
            StorageConstants.SUITE_NAME,
            Context.MODE_PRIVATE
        )
        stringStorageSpy = spyk(StringStorage(sharedPreferences))
        stringStorageSpy.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, null)
        legacyDBOpenHelper = LegacyDBOpenHelper(applicationContext)
        requestContextSpy = spyk(RequestContext())
        legacySDKMigrationState = LegacySDKMigrationState(
            legacySharedPreferencesWrapper = LegacySharedPreferencesWrapper(
                legacySharedPreferences,
                sharedPreferencesCrypto
            ),
            legacyDBOpenHelper = legacyDBOpenHelper,
            requestContext = requestContextSpy,
            stringStorage = stringStorageSpy,
            ioDispatcher = Dispatchers.IO,
            sdkLogger = mockSdkLogger
        )

        stringStorageSpy.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, null)
        legacySharedPreferences.edit().clear().apply()
        stringStorageSpy.put("mobile_engage_contact_token", null)
        stringStorageSpy.put("mobile_engage_refresh_token", null)
        stringStorageSpy.put("mobile_engage_client_state", null)
        stringStorageSpy.put("mobile_engage_push_token", null)
        stringStorageSpy.put("mobile_engage_device_event_state", null)
        clearAllMocks()
    }

    @Test
    fun active_shouldMigrateLegacyData() = runTest {
        legacyDBOpenHelper.writableDatabase.transaction {
            execSQL(
                "CREATE TABLE IF NOT EXISTS hardware_identification (\n" +
                        "hardware_id TEXT,\n" +
                        "encrypted_hardware_id TEXT,\n" +
                        "salt TEXT,\n" +
                        "iv TEXT\n" +
                        "); "
            )
            execSQL(
                "INSERT INTO hardware_identification (hardware_id) VALUES ('$TEST_CLIENT_ID');"
            )
        }
        legacySharedPreferences.edit().putString(
            "mobile_engage_contact_token", tryEncrypt(TEST_CONTACT_TOKEN)
        ).apply()
        legacySharedPreferences.edit().putString(
            "mobile_engage_refresh_token", tryEncrypt(TEST_REFRESH_TOKEN)
        ).apply()
        legacySharedPreferences.edit().putString(
            "mobile_engage_client_state", tryEncrypt(TEST_CLIENT_STATE)
        ).apply()
        legacySharedPreferences.edit().putString(
            "mobile_engage_push_token", tryEncrypt(TEST_PUSH_TOKEN)
        ).apply()
        legacySharedPreferences.edit().putString(
            "mobile_engage_device_event_state", tryEncrypt(TEST_DEVICE_EVENT_STATE)
        ).apply()

        legacySDKMigrationState.active()

        coVerify {
            stringStorageSpy.put(CLIENT_ID_STORAGE_KEY, TEST_CLIENT_ID)
            requestContextSpy.contactToken = TEST_CONTACT_TOKEN
            requestContextSpy.refreshToken = TEST_REFRESH_TOKEN
            requestContextSpy.clientState = TEST_CLIENT_STATE
            stringStorageSpy.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, TEST_PUSH_TOKEN)
            requestContextSpy.deviceEventState = TEST_DEVICE_EVENT_STATE
            stringStorageSpy.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, "true")
        }
    }

    @Test
    fun active_shouldMigrateLegacyData_evenIfPartOfTheMigrationFails_andThereIsMissingData() =
        runTest {
            legacyDBOpenHelper.writableDatabase.transaction {
                execSQL(
                    "CREATE TABLE IF NOT EXISTS hardware_identification (\n" +
                            "hardware_id TEXT,\n" +
                            "encrypted_hardware_id TEXT,\n" +
                            "salt TEXT,\n" +
                            "iv TEXT\n" +
                            "); "
                )
                execSQL(
                    "INSERT INTO hardware_identification (hardware_id) VALUES ('$TEST_CLIENT_ID');"
                )
            }
            legacySharedPreferences.edit().putString(
                "mobile_engage_contact_token", tryEncrypt(TEST_CONTACT_TOKEN)
            ).apply()
            legacySharedPreferences.edit().putString(
                "mobile_engage_client_state", "INVALID_ENCRYPTED_VALUE"
            ).apply()

            legacySDKMigrationState.active()

            coVerify {
                stringStorageSpy.get(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY)
                stringStorageSpy.put(CLIENT_ID_STORAGE_KEY, TEST_CLIENT_ID)
                requestContextSpy.contactToken = TEST_CONTACT_TOKEN
                stringStorageSpy.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, "true")
            }
            confirmVerified(requestContextSpy)
            confirmVerified(stringStorageSpy)
        }

    @Test
    fun active_shouldReturn_whenDataHasAlreadyBeenMigrated() = runTest {
        stringStorageSpy.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, "true")
        clearMocks(stringStorageSpy)

        legacySDKMigrationState.active()

        coVerify { stringStorageSpy.get(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY) }
        confirmVerified(stringStorageSpy)
    }

    @Test
    fun active_shouldNotLeakAnyException() = runTest {
        coEvery { mockSdkLogger.debug("Starting migration from legacy Android SDK") } throws Exception(
            "Test exception"
        )

        legacySDKMigrationState.active()

        coVerify { mockSdkLogger.debug("Starting migration from legacy Android SDK")  }
    }


    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            return createSecretKey()
        }

        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    private fun tryEncrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(value.toByteArray())
        val iv = cipher.iv
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT)
        return "$ivBase64$encryptedBase64"
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}