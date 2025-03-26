package com.emarsys.api.push

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal open class LoggingPush(
    private val storage: StringStorageApi,
    private val logger: Logger,
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        storage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, pushToken)
        val entry = LogEntry.createMethodNotAllowed(
            this, this::registerPushToken.name, buildJsonObject {
                put("pushToken", JsonPrimitive(pushToken))
            }
        )
        logger.debug(entry)
    }

    override suspend fun clearPushToken() {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::clearPushToken.name
        )
        logger.debug(entry)
    }

    override suspend fun getPushToken(): String? {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
        return null
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}