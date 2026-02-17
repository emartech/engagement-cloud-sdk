package com.sap.ec.init.states

import com.sap.ec.SdkConstants
import com.sap.ec.api.push.PushConstants
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.storage.KeychainStorageApi
import com.sap.ec.fake.FakeStringStorage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class LegacySDKMigrationStateTests {

    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var fakeStringStorage: FakeStringStorage
    private lateinit var mockKeychainStorage: KeychainStorageApi
    private lateinit var mockLogger: Logger
    private lateinit var migrationState: LegacySDKMigrationState

    private companion object {
        const val TEST_CLIENT_ID = "test_client_id"
        const val TEST_CONTACT_TOKEN = "test_contact_token"
        const val TEST_REFRESH_TOKEN = "test_refresh_token"
        const val TEST_CLIENT_STATE = "test_client_state"
        const val TEST_DEVICE_EVENT_STATE = "test_device_event_state"

        val DEVICE_TOKEN_BYTES = byteArrayOf(
            0x1a, 0x2b, 0x3c, 0x4d, 0x5e, 0x6f, 0x70.toByte(), 0x81.toByte(),
            0x92.toByte(), 0xa3.toByte(), 0xb4.toByte(), 0xc5.toByte(), 0xd6.toByte(), 0xe7.toByte(), 0xf8.toByte(), 0x09,
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
            0x88.toByte(), 0x99.toByte(), 0xaa.toByte(), 0xbb.toByte(), 0xcc.toByte(), 0xdd.toByte(), 0xee.toByte(), 0xff.toByte()
        )
        const val PUSH_TOKEN = "1a2b3c4d5e6f708192a3b4c5d6e7f80900112233445566778899aabbccddeeff"

        fun createNSDataFromBytes(bytes: ByteArray): NSData {
            return bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
        }
    }

    @BeforeTest
    fun setup() {
        mockRequestContext = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        fakeStringStorage = FakeStringStorage()
        mockKeychainStorage = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        migrationState = LegacySDKMigrationState(
            requestContext = mockRequestContext,
            sdkContext = mockSdkContext,
            stringStorage = fakeStringStorage,
            keychainStorage = mockKeychainStorage,
            sdkLogger = mockLogger
        )
    }

    @Test
    fun name_shouldReturn_migrationState() {
        migrationState.name shouldBe "legacySDKMigrationState"
    }

    @Test
    fun prepare_shouldNotThrow() {
        migrationState.prepare()
    }

    @Test
    fun relax_shouldNotThrow() {
        migrationState.relax()
    }

    @Test
    fun active_shouldNotReadFromKeychain_whenMigrationAlreadyDone() = runTest {
        fakeStringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockKeychainStorage.readString("kHardwareIdKey") }
        verify(VerifyMode.exactly(0)) { mockKeychainStorage.readData("EMSPushTokenKey") }
        verify(VerifyMode.exactly(0)) { mockKeychainStorage.readString("kCONTACT_TOKEN") }
        verify(VerifyMode.exactly(0)) { mockKeychainStorage.readString("kREFRESH_TOKEN") }
    }

    @Test
    fun active_shouldSetMigrationDone() = runTest {
        val result = migrationState.active()

        fakeStringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) shouldBe "true"

        result shouldBe Result.success(Unit)
    }


    @Test
    fun active_shouldMigrateClientId_fromKeychain() = runTest {
        every { mockKeychainStorage.readString("kHardwareIdKey") } returns TEST_CLIENT_ID

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe TEST_CLIENT_ID
    }

    @Test
    fun active_shouldNotSetClientId_whenNotFoundInKeychain() = runTest {
        every { mockKeychainStorage.readString("kHardwareIdKey") } returns null

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe null
    }

    @Test
    fun active_shouldMigratePushToken_fromKeychain() = runTest {
        val tokenData =
            createNSDataFromBytes(
                DEVICE_TOKEN_BYTES
            )
        every { mockKeychainStorage.readData("EMSPushTokenKey") } returns tokenData

        migrationState.active()

        fakeStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe PUSH_TOKEN
    }

    @Test
    fun active_shouldMigrateContactToken_fromKeychain() = runTest {
        every { mockKeychainStorage.readString("kCONTACT_TOKEN") } returns TEST_CONTACT_TOKEN

        migrationState.active()

        verify { mockRequestContext.contactToken =
            TEST_CONTACT_TOKEN
        }
    }

    @Test
    fun active_shouldNotSetContactToken_whenNotFoundInKeychain() = runTest {
        every { mockKeychainStorage.readString("kCONTACT_TOKEN") } returns null

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.contactToken = any() }
    }

    @Test
    fun active_shouldMigrateRefreshToken_fromKeychain() = runTest {
        every { mockKeychainStorage.readString("kREFRESH_TOKEN") } returns TEST_REFRESH_TOKEN

        migrationState.active()

        verify { mockRequestContext.refreshToken =
            TEST_REFRESH_TOKEN
        }
    }

    @Test
    fun active_shouldNotSetRefreshToken_whenNotFoundInKeychain() = runTest {
        every { mockKeychainStorage.readString("kREFRESH_TOKEN") } returns null

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.refreshToken = any() }
    }

    @Test
    fun active_shouldMigrateClientState_fromKeychain() = runTest {
        every { mockKeychainStorage.readString("kCLIENT_STATE") } returns TEST_CLIENT_STATE

        migrationState.active()

        verify { mockRequestContext.clientState =
            TEST_CLIENT_STATE
        }
    }

    @Test
    fun active_shouldNotSetClientState_whenNotFoundInKeychain() = runTest {
        every { mockKeychainStorage.readString("kCLIENT_STATE") } returns null

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.clientState = any() }
    }

    @Test
    fun active_shouldMigrateDeviceEventState_fromKeychain() = runTest {
        every { mockKeychainStorage.readString("DEVICE_EVENT_STATE_KEY") } returns TEST_DEVICE_EVENT_STATE

        migrationState.active()

        verify { mockRequestContext.deviceEventState =
            TEST_DEVICE_EVENT_STATE
        }
    }

    @Test
    fun active_shouldNotSetDeviceEventState_whenNotFoundInKeychain() = runTest {
        every { mockKeychainStorage.readString("DEVICE_EVENT_STATE_KEY") } returns null

        migrationState.active()

        verify(VerifyMode.exactly(0)) { mockRequestContext.deviceEventState = any() }
    }

    @Test
    fun active_shouldMigrateAllValues_whenAllExist() = runTest {
        val pushTokenData =
            createNSDataFromBytes(
                DEVICE_TOKEN_BYTES
            )

        every { mockKeychainStorage.readString("kHardwareIdKey") } returns TEST_CLIENT_ID
        every { mockKeychainStorage.readString("kCONTACT_TOKEN") } returns TEST_CONTACT_TOKEN
        every { mockKeychainStorage.readString("kREFRESH_TOKEN") } returns TEST_REFRESH_TOKEN
        every { mockKeychainStorage.readString("kCLIENT_STATE") } returns TEST_CLIENT_STATE
        every { mockKeychainStorage.readData("EMSPushTokenKey") } returns pushTokenData
        every { mockKeychainStorage.readString("DEVICE_EVENT_STATE_KEY") } returns TEST_DEVICE_EVENT_STATE

        migrationState.active()

        fakeStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) shouldBe TEST_CLIENT_ID
        verify { mockRequestContext.contactToken =
            TEST_CONTACT_TOKEN
        }
        verify { mockRequestContext.refreshToken =
            TEST_REFRESH_TOKEN
        }
        verify { mockRequestContext.clientState =
            TEST_CLIENT_STATE
        }
        fakeStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) shouldBe PUSH_TOKEN
        verify { mockRequestContext.deviceEventState =
            TEST_DEVICE_EVENT_STATE
        }
        fakeStringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) shouldBe "true"
    }
}
