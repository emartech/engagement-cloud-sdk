package com.emarsys.mobileengage.push

import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.LoggingPush
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject


internal class IosLoggingPush(
    private val logger: Logger,
    storage: StringStorageApi,
    private val sdkDispatcher: CoroutineDispatcher
) : LoggingPush(storage, logger), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            CoroutineScope(sdkDispatcher).launch {
                logger.debug(entry)
            }
            return null
        }
        set(_) {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            CoroutineScope(sdkDispatcher).launch {
                logger.debug(entry)
            }
        }
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            CoroutineScope(sdkDispatcher).launch {
                logger.debug(entry)
            }
            return object : NSObject(), UNUserNotificationCenterDelegateProtocol {}
        }

    override suspend fun handleSilentMessageWithUserInfo(userInfo: BasicPushUserInfo) {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}