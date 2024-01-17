package com.emarsys.service

import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class EmarsysHuaweiMessagingService : HmsMessageService() {
    private companion object {
        const val PUSH_TOKEN_INTENT_FILTER_ACTION = "com.emarsys.sdk.PUSH_TOKEN"
        const val PUSH_TOKEN_INTENT_KEY = "pushToken"
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

    }
}