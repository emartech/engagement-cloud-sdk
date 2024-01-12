package com.emarsys.service

import android.content.Context
import com.huawei.hms.push.RemoteMessage

object EmarsysHuaweiMessagingServiceUtils {

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        return false
    }
}