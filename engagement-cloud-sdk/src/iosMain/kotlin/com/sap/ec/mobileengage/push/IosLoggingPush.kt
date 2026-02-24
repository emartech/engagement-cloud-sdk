package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.LoggingPush
import com.sap.ec.api.push.NotificationCenterDelegateRegistration
import com.sap.ec.api.push.NotificationCenterDelegateRegistrationOptions
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
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

    override val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::registeredNotificationCenterDelegates.name)
            CoroutineScope(sdkDispatcher).launch {
                logger.debug(entry)
            }
            return emptyList()
        }

    override fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions
    ) {
        val entry = LogEntry.createMethodNotAllowed(this, this::registerNotificationCenterDelegate.name)
        CoroutineScope(sdkDispatcher).launch {
            logger.debug(entry)
        }
    }

    override fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol) {
        val entry = LogEntry.createMethodNotAllowed(this, this::unregisterNotificationCenterDelegate.name)
        CoroutineScope(sdkDispatcher).launch {
            logger.debug(entry)
        }
    }

    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::userNotificationCenterDelegate.name)
            CoroutineScope(sdkDispatcher).launch {
                logger.debug(entry)
            }
            return object : NSObject(), UNUserNotificationCenterDelegateProtocol {}
        }

    override suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo) {
        val entry = LogEntry.createMethodNotAllowed(this, this::handleSilentMessageWithUserInfo.name)
        logger.debug(entry)
    }
}