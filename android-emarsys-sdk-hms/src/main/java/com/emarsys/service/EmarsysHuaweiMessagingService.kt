package com.emarsys.service

import android.content.Context
import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class EmarsysHuaweiMessagingService() : HmsMessageService() {
    private var application: Context? = baseContext?.applicationContext

    companion object {
        const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION =
            "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
        const val EMS_MESSAGE_KEY = "ems_msg"
        internal val messagingServices = mutableListOf<Pair<Boolean, HmsMessageService>>()

        fun registerMessagingService(
            messagingService: HmsMessageService,
            includeEmarsysMessages: Boolean = false
        ) {
            messagingServices.add(Pair(includeEmarsysMessages, messagingService))
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val intent = Intent().apply {
            action = PUSH_TOKEN_INTENT_FILTER_ACTION
            putExtra(PUSH_TOKEN_INTENT_KEY, token)
        }
        application?.sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        messagingServices
            .filter { it.first || !remoteMessage.dataOfMap.containsKey(EMS_MESSAGE_KEY) }
            .forEach { it.second.onMessageReceived(remoteMessage) }

        val intent = Intent().apply {
            action = PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION
            putExtra(
                PUSH_MESSAGE_PAYLOAD_INTENT_KEY,
                HmsRemoteMessageMapper.map(remoteMessage.dataOfMap).toString()
            )
        }

        application?.sendBroadcast(intent)
    }
}