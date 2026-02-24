package com.sap.ec.api.push

import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import kotlin.experimental.ExperimentalObjCName

/**
 * Represents a registered notification delegate along with its configuration options.
 *
 * @param delegate The notification delegate implementing [UNUserNotificationCenterDelegateProtocol].
 * @param options Configuration options controlling how notifications are forwarded to this delegate.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("NotificationCenterDelegateRegistration")
data class NotificationCenterDelegateRegistration(
    val delegate: UNUserNotificationCenterDelegateProtocol,
    val options: NotificationCenterDelegateRegistrationOptions = NotificationCenterDelegateRegistrationOptions()
)
