package com.sap.ec.mobileengage.push

import com.sap.ec.core.device.notification.PermissionState
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger

internal class JsLoggingPush(
    private val logger: Logger,
) : JsPushInstance {

    override suspend fun subscribe(): Result<Unit> {
        val entry = LogEntry.createMethodNotAllowed(this, this::subscribe.name)
        logger.debug(entry)
        return Result.success(Unit)
    }

    override suspend fun unsubscribe(): Result<Unit> {
        val entry = LogEntry.createMethodNotAllowed(this, this::unsubscribe.name)
        logger.debug(entry)
        return Result.success(Unit)
    }

    override suspend fun isSubscribed(): Boolean {
        val entry = LogEntry.createMethodNotAllowed(this, this::isSubscribed.name)
        logger.debug(entry)
        return false
    }

    override suspend fun getPermissionState(): PermissionState {
        val entry = LogEntry.createMethodNotAllowed(this, this::getPermissionState.name)
        logger.debug(entry)
        return PermissionState.Denied
    }

    override suspend fun activate() {
    }
}

