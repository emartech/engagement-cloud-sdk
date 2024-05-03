package com.emarsys.service

import android.content.Intent
import com.emarsys.api.push.PushApi
import com.emarsys.service.EmarsysFirebaseMessagingService.Companion.messagingServices
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class EmarsysFirebaseMessagingService : FirebaseMessagingService(), PushApi {
    companion object {
        private const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        private const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        private const val EMS_VERSION_KEY = "ems.version"
        internal val messagingServices = mutableListOf<Pair<Boolean, FirebaseMessagingService>>()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val intent = Intent().apply {
            action = PUSH_TOKEN_INTENT_FILTER_ACTION
            putExtra(PUSH_TOKEN_INTENT_KEY, token)
        }
        sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        messagingServices
            .filter { it.first || !remoteMessage.data.containsKey(EMS_VERSION_KEY) }
            .forEach { it.second.onMessageReceived(remoteMessage) }
    }
}

fun PushApi.registerMessagingService(
    messagingService: FirebaseMessagingService,
    includeEmarsysMessages: Boolean = false
) {
    messagingServices.add(Pair(includeEmarsysMessages, messagingService))
}