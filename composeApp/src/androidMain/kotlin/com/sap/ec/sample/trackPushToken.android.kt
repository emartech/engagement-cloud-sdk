package com.sap.ec.sample


import com.sap.ec.EngagementCloud

actual suspend fun registerPushToken(token: String) {
    EngagementCloud.push.registerToken(token = token)
}