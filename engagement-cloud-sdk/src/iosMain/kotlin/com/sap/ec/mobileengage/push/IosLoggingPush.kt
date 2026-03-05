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

internal class IosLoggingPush(
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher,
    storage: StringStorageApi,
    private val iosPushInternal: IosPushInstance
) : LoggingPush(storage, logger), IosPushInstance {

    override val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>
        get() = iosPushInternal.registeredNotificationCenterDelegates

    override fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions
    ) {
        CoroutineScope(dispatcher).launch {
            logger.trace("registerNotificationCenterDelegate was called")
        }
        iosPushInternal.registerNotificationCenterDelegate(delegate, options)
    }

    override fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol) {
        CoroutineScope(dispatcher).launch {
            logger.trace("unregisterNotificationCenterDelegate was called")
        }
        iosPushInternal.unregisterNotificationCenterDelegate(delegate)
    }

    override val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() {
            CoroutineScope(dispatcher).launch {
                logger.trace("userNotificationCenterDelegate was called")
            }
            return iosPushInternal.userNotificationCenterDelegate
        }

    override suspend fun handleSilentMessageWithUserInfo(userInfo: SilentPushUserInfo) {
        val entry = LogEntry.createMethodNotAllowed(this, this::handleSilentMessageWithUserInfo.name)
        logger.debug(entry)
    }
}