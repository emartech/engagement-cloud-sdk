package com.emarsys.service.hms

import android.content.Intent
import com.emarsys.service.hms.model.HuaweiMessagingServiceRegistrationOptions
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class EmarsysHuaweiMessagingService() : HmsMessageService() {

    companion object {
        const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION =
            "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
        const val EMS_KEY = "ems"

        internal val messagingServices =
            mutableListOf<Pair<HuaweiMessagingServiceRegistrationOptions, HmsMessageService>>()

        fun registerMessagingService(
            messagingService: HmsMessageService,
            registrationOptions: HuaweiMessagingServiceRegistrationOptions = HuaweiMessagingServiceRegistrationOptions()
        ) {
            messagingServices.add(Pair(registrationOptions, messagingService))
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val intent = Intent().apply {
            action = PUSH_TOKEN_INTENT_FILTER_ACTION
            putExtra(PUSH_TOKEN_INTENT_KEY, token)
        }
        applicationContext.sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        messagingServices
            .filter {
                it.first.includeEmarsysMessages || !remoteMessage.dataOfMap.containsKey(EMS_KEY)
            }
            .forEach { it.second.onMessageReceived(remoteMessage) }

        val intent = Intent().apply {
            action = PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION
            putExtra(
                PUSH_MESSAGE_PAYLOAD_INTENT_KEY,
                remoteMessage.data
            )
            setPackage(applicationContext.packageName)
        }

        applicationContext.sendBroadcast(intent)
    }
}