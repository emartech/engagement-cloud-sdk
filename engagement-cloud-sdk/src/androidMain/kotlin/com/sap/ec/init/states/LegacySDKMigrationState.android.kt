package com.sap.ec.init.states

import android.database.sqlite.SQLiteOpenHelper
import com.sap.ec.SdkConstants
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.db_migration.LegacySharedPreferencesWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.use

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class LegacySDKMigrationState(
    private val legacySharedPreferencesWrapper: LegacySharedPreferencesWrapper,
    private val legacyDBOpenHelper: SQLiteOpenHelper,
    private val requestContext: RequestContextApi,
    private val stringStorage: StringStorageApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger,
) : State {
    actual override val name: String = "legacySDKMigrationState"

    actual override fun prepare() {}

    actual override suspend fun active(): Result<Unit> {
        return try {
            if (stringStorage.get(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY) != null) {
                return Result.success(Unit)
            }

            sdkLogger.debug("Starting migration from legacy Android SDK")

            try {
                val db = withContext(Dispatchers.IO) { legacyDBOpenHelper.readableDatabase }
                db.query(
                    "hardware_identification",
                    arrayOf("hardware_id"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "1",
                ).use {
                    if (it.moveToFirst()) {
                        it.getString(0)?.let { legacyClientId ->
                            stringStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, legacyClientId)
                            sdkLogger.debug("Migrated legacy client id.")
                        }
                    }
                }
            } catch (exception: Exception) {
                sdkLogger.debug("Migrating legacy clientId failed: ${exception.message}")
            }

            migrateLegacySharedPrefKey("mobile_engage_contact_token") {
                requestContext.contactToken = it
            }

            migrateLegacySharedPrefKey("mobile_engage_refresh_token") {
                requestContext.refreshToken = it
            }

            migrateLegacySharedPrefKey("mobile_engage_client_state") {
                requestContext.clientState = it
            }

            migrateLegacySharedPrefKey("mobile_engage_push_token") {
                stringStorage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, it)
            }

            migrateLegacySharedPrefKey("mobile_engage_device_event_state") {
                requestContext.deviceEventState = it
            }

            stringStorage.put(SdkConstants.LEGACY_SDK_MIGRATION_DONE_KEY, "true")
            sdkLogger.debug("Migration from legacy SDK completed")
            Result.success(Unit)
        } catch (exception: Exception) {
            sdkLogger.error("Migration from legacy SDK failed.", exception)
            Result.failure(exception)
        }
    }

    actual override fun relax() {}

    suspend fun migrateLegacySharedPrefKey(key: String, migrateAction: (String) -> Unit) {
        try {
            legacySharedPreferencesWrapper.getDecryptedString(key)?.let {
                migrateAction(it)
                sdkLogger.debug("Migrated legacy $key.")
            }
        } catch (exception: Exception) {
            sdkLogger.error("Migrating legacy $key failed.", exception)
        }

    }
}
