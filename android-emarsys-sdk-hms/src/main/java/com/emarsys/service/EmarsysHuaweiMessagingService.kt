package com.emarsys.service

import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class EmarsysHuaweiMessagingService : HmsMessageService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

    }
}