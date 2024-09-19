package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.push.LoggingPush
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject


class IosLoggingPush(private val logger: Logger,
                     notificationEvents: MutableSharedFlow<AppEvent>,
) : LoggingPush(logger, notificationEvents), IosPushInstance {
    override var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            logger.log(entry, LogLevel.Debug)
            return null
        }
        set(_) {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            logger.log(entry, LogLevel.Debug)
        }
    override val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol
        get() {
            val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
            logger.log(entry, LogLevel.Debug)
            return object: NSObject(), UNUserNotificationCenterDelegateProtocol {}
        }

    override fun registerEmarsysNotificationCenterDelegate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.log(entry, LogLevel.Debug)
    }
}