package com.emarsys.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class EmarsysFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        private const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        private const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION =
            "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
        private const val EMS_VERSION_KEY = "ems.version"
        internal val messagingServices = mutableListOf<Pair<Boolean, FirebaseMessagingService>>()

        fun registerMessagingService(
            messagingService: FirebaseMessagingService,
            includeEmarsysMessages: Boolean = false
        ) {
            messagingServices.add(Pair(includeEmarsysMessages, messagingService))
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("token received: $token")
        val intent = Intent().apply {
            action = PUSH_TOKEN_INTENT_FILTER_ACTION
            putExtra(PUSH_TOKEN_INTENT_KEY, token)
        }
        applicationContext.sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("message received: ${remoteMessage.messageId}")
        messagingServices
            .filter { it.first || !remoteMessage.data.containsKey(EMS_VERSION_KEY) }
            .forEach { it.second.onMessageReceived(remoteMessage) }

        val intent = Intent().apply {
            action = PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION
            putExtra(
                PUSH_MESSAGE_PAYLOAD_INTENT_KEY,
                FirebaseRemoteMessageMapper.map(remoteMessage.data).toString()
            )
        }
        applicationContext.sendBroadcast(intent)
    }
}
