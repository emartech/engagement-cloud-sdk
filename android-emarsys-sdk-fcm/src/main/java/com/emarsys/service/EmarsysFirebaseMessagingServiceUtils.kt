package com.emarsys.service

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

object EmarsysFirebaseMessagingServiceUtils {

    @JvmStatic
    fun handleMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
        return false
    }
}