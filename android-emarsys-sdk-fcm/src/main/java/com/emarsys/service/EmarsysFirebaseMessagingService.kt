package com.emarsys.service

import android.content.Intent
import com.emarsys.service.model.FirebaseMessagingServiceRegistrationOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.serialization.json.Json


class EmarsysFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        private const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        private const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION =
            "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
        private const val EMS_VERSION_KEY = "ems.version"

        internal val messagingServices =
            mutableListOf<Pair<FirebaseMessagingServiceRegistrationOptions, FirebaseMessagingService>>()

        fun registerMessagingService(
            messagingService: FirebaseMessagingService,
            registrationOptions: FirebaseMessagingServiceRegistrationOptions = FirebaseMessagingServiceRegistrationOptions()
        ) {
            messagingServices.add(Pair(registrationOptions, messagingService))
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("token received: $token")
        val intent = createIntent(PUSH_TOKEN_INTENT_FILTER_ACTION, PUSH_TOKEN_INTENT_KEY, token)
        applicationContext.sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("message received: ${remoteMessage.messageId}")
        messagingServices
            .filter {
                it.first.includeEmarsysMessages || !isEmarsysMessage(remoteMessage.data)
            }
            .forEach { it.second.onMessageReceived(remoteMessage) }
        if (isEmarsysMessage(remoteMessage.data)) {
            val messagePayload = Json.encodeToString(remoteMessage.data)
            val intent = createIntent(
                PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION,
                PUSH_MESSAGE_PAYLOAD_INTENT_KEY,
                messagePayload
            )
            applicationContext.sendBroadcast(intent)
        }
    }

    private fun createIntent(action: String, extraKey: String, extraValue: String): Intent {
        return Intent().apply {
            this.action = action
            putExtra(extraKey, extraValue)
            setPackage(applicationContext.packageName)
        }
    }

    private fun isEmarsysMessage(remoteMessage: Map<String, String>): Boolean {
        return remoteMessage.containsKey(EMS_VERSION_KEY)
    }

}
