package com.emarsys.api.push

import com.emarsys.core.exceptions.PreconditionFailedException
import io.ktor.utils.io.CancellationException
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

interface IosPushApi {
    var customerUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol?
    val emarsysUserNotificationCenterDelegate: UNUserNotificationCenterDelegateProtocol

    @Throws(CancellationException::class)
    suspend fun getToken(): String?

    @Throws(PreconditionFailedException::class, CancellationException::class)
    suspend fun handleSilentMessageWithUserInfo(rawUserInfo: Map<String, Any>)

    @Throws(CancellationException::class)
    suspend fun registerToken(token: String)

    @Throws(CancellationException::class)
    suspend fun clearToken()
}