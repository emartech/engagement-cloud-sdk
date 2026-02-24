package com.sap.ec.api.push

import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import io.ktor.utils.io.CancellationException
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushApi {
    /**
     * The list of registered notification delegates.
     * Use [registerNotificationCenterDelegate] and [unregisterNotificationCenterDelegate] to manage delegates.
     */
    val registeredNotificationCenterDelegates: List<NotificationCenterDelegateRegistration>

    /**
     * The SDK's notification center delegate. Assign this to `UNUserNotificationCenter.current().delegate`
     * to enable push notification handling.
     */
    val userNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    /**
     * Registers a notification delegate with configuration options.
     *
     * @param delegate The delegate implementing [UNUserNotificationCenterDelegateProtocol].
     * @param options Configuration options. Use [NotificationCenterDelegateRegistrationOptions.includeEngagementCloudMessages]
     * to control whether the delegate receives Engagement Cloud notifications.
     */
    fun registerNotificationCenterDelegate(
        delegate: UNUserNotificationCenterDelegateProtocol,
        options: NotificationCenterDelegateRegistrationOptions = NotificationCenterDelegateRegistrationOptions()
    )

    /**
     * Unregisters a previously registered notification delegate.
     *
     * @param delegate The delegate to unregister.
     */
    fun unregisterNotificationCenterDelegate(delegate: UNUserNotificationCenterDelegateProtocol)

    /**
     * Retrieves the current push notification token.
     *
     * @return The push token if available, or null if not registered.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun getToken(): String?

    /**
     * Handles a silent push notification.
     *
     * @param rawUserInfo The notification user info dictionary.
     * @throws PreconditionFailedException If the SDK is not properly initialized.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(PreconditionFailedException::class, CancellationException::class)
    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>)

    /**
     * Registers a push notification token with Engagement Cloud.
     *
     * @param token The device push token to register.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun registerToken(token: String)

    /**
     * Clears the registered push notification token.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun clearToken()
}