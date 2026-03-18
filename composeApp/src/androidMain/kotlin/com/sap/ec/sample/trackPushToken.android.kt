package com.sap.ec.sample

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.sap.ec.EngagementCloud
import kotlinx.coroutines.tasks.await

actual suspend fun registerPushToken(token: String) {
    EngagementCloud.push.registerToken(token = token)
}