package com.emarsys.db_migration.states

import android.database.sqlite.SQLiteOpenHelper
import com.emarsys.SdkConstants
import com.emarsys.SdkConstants.CLIENT_ID_STORAGE_KEY
import com.emarsys.api.push.PushConstants
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.db_migration.LegacySharedPreferencesWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            if (stringStorage.get(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY) != null) {
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
                            stringStorage.put(CLIENT_ID_STORAGE_KEY, legacyClientId)
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

            stringStorage.put(SdkConstants.LEGACY_DB_MIGRATION_DONE_KEY, "true")
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




