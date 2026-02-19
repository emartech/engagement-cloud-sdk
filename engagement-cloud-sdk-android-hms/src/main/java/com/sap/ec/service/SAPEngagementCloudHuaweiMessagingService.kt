package com.sap.ec.service

import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.sap.ec.model.HuaweiMessagingServiceRegistrationOptions
import org.jetbrains.annotations.ApiStatus

class SAPEngagementCloudHuaweiMessagingService() : HmsMessageService() {

    companion object Companion {
        const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.sap.ec.sdk.PUSH_TOKEN"
        const val PUSH_TOKEN_INTENT_KEY = "pushToken"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_KEY = "pushPayload"
        const val PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION =
            "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
        const val EMS_KEY = "ems"

        internal val messagingServices =
            mutableListOf<Pair<HuaweiMessagingServiceRegistrationOptions, HmsMessageService>>()

        @ApiStatus.Experimental
        fun registerMessagingService(
            messagingService: HmsMessageService,
            registrationOptions: HuaweiMessagingServiceRegistrationOptions
        ) {
            messagingServices.add(Pair(registrationOptions, messagingService))
        }

        fun registerMessagingService(
            messagingService: HmsMessageService
        ) {
            messagingServices.add(
                Pair(
                    HuaweiMessagingServiceRegistrationOptions(),
                    messagingService
                )
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val intent = createIntent(PUSH_TOKEN_INTENT_FILTER_ACTION, PUSH_TOKEN_INTENT_KEY, token)
        applicationContext.sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        messagingServices
            .filter {
                it.first.includeEngagementCloudMessages || !remoteMessage.dataOfMap.containsKey(
                    EMS_KEY
                )
            }
            .forEach { it.second.onMessageReceived(remoteMessage) }

        val intent = createIntent(
            PUSH_MESSAGE_PAYLOAD_INTENT_FILTER_ACTION,
            PUSH_MESSAGE_PAYLOAD_INTENT_KEY,
            remoteMessage.data
        )

        applicationContext.sendBroadcast(intent)
    }

    private fun createIntent(action: String, extraKey: String, extraValue: String): Intent {
        return Intent().apply {
            this.action = action
            putExtra(extraKey, extraValue)
            setPackage(applicationContext.packageName)
        }
    }
}