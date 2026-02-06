package com.emarsys.init.states

import com.emarsys.SdkConstants
import com.emarsys.api.push.PushConstants
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.KeychainStorageApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.util.runCatchingWithoutCancellation
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal actual class LegacySDKMigrationState(
    private val requestContext: RequestContextApi,
    private val sdkContext: SdkContextApi,
    private val stringStorage: StringStorageApi,
    private val keychainStorage: KeychainStorageApi,
    private val sdkLogger: Logger
) : State {
    actual override val name: String = "legacySDKMigrationState"

    private val allUserDefaults: List<NSUserDefaults> by lazy {
        LEGACY_SUITE_NAMES
            .map { NSUserDefaults(suiteName = it) } + NSUserDefaults.standardUserDefaults
    }

    actual override fun prepare() {}

    actual override suspend fun active(): Result<Unit> {
        if (isMigrationAlreadyDone()) {
            return Result.success(Unit)
        }

        sdkLogger.debug("Starting migration from legacy iOS SDK")

        runCatchingWithoutCancellation {
            migrateClientId()
            migrateContactToken()
            migrateRefreshToken()
            migrateClientState()
            migratePushToken()
            migrateDeviceEventState()
        }.onFailure {
            sdkLogger.error("Migration from legacy iOS SDK failed: ${it.message}")
        }

        stringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")
        return Result.success(Unit)
    }

    actual override fun relax() {}

    private fun isMigrationAlreadyDone(): Boolean =
        stringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) != null

    private fun find(key: String): String? = keychainStorage.readString(key) ?: findInUserDefaults(key)


    private suspend fun migrateClientId() {
        runCatchingWithoutCancellation {
            find(KEY_HARDWARE_ID)?.let {
                stringStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, it)
                sdkLogger.debug("Migrated legacy clientId")
            }
        }
    }

    private suspend fun migrateContactToken() {
        runCatchingWithoutCancellation {
            find(KEY_CONTACT_TOKEN)?.let {
                requestContext.contactToken = it
                sdkLogger.debug("Migrated legacy contactToken")
            }
        }
    }

    private suspend fun migrateRefreshToken() {
        runCatchingWithoutCancellation {
            find(KEY_REFRESH_TOKEN)?.let {
                requestContext.refreshToken = it
                sdkLogger.debug("Migrated legacy refreshToken")
            }
        }
    }

    private suspend fun migrateClientState() {
        runCatchingWithoutCancellation {
            find(KEY_CLIENT_STATE)?.let {
                requestContext.clientState = it
                sdkLogger.debug("Migrated legacy clientState")
            }
        }
    }

    private suspend fun migratePushToken() {
        runCatchingWithoutCancellation {
            val pushTokenData = keychainStorage.readData(KEY_PUSH_TOKEN)
                ?: findDataInUserDefaults(KEY_PUSH_TOKEN)
            pushTokenData?.let {
                stringStorage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, it.toHexString())
                sdkLogger.debug("Migrated legacy pushToken")
            }
        }
    }

    private suspend fun migrateDeviceEventState() {
        runCatchingWithoutCancellation {
            find(KEY_DEVICE_EVENT_STATE)?.let {
                requestContext.deviceEventState = it
                sdkLogger.debug("Migrated legacy deviceEventState")
            }
        }
    }

    private fun findInUserDefaults(key: String): String? {
        for (suite in allUserDefaults) {
            suite.stringForKey(key)?.takeIf { it.isNotEmpty() }?.let { return it }
            suite.dataForKey(key)?.asString()?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        return null
    }

    private fun findDataInUserDefaults(key: String): NSData? =
        allUserDefaults.firstNotNullOfOrNull { it.dataForKey(key) }

    @OptIn(BetaInteropApi::class)
    private fun NSData.asString(): String? =
        NSString.create(this, NSUTF8StringEncoding)?.toString()

    private fun NSData.toHexString(): String {
        val bytes = this.bytes?.reinterpret<ByteVar>() ?: return ""
        val length = this.length.toInt()
        return buildString(length * 2) {
            for (i in 0 until length) {
                val byte = bytes[i].toInt() and 0xFF
                append(byte.toString(16).padStart(2, '0'))
            }
        }
    }

    private companion object {
        val LEGACY_SUITE_NAMES = listOf(
            "com.emarsys.mobileengage",
            "com.emarsys.core",
            "com.emarsys.sdk",
            "com.emarsys.predict"
        )

        const val KEY_HARDWARE_ID = "kHardwareIdKey"
        const val KEY_PUSH_TOKEN = "EMSPushTokenKey"
        const val KEY_CONTACT_TOKEN = "kCONTACT_TOKEN"
        const val KEY_REFRESH_TOKEN = "kREFRESH_TOKEN"
        const val KEY_CLIENT_STATE = "kCLIENT_STATE"
        const val KEY_DEVICE_EVENT_STATE = "DEVICE_EVENT_STATE_KEY"
    }
}
