package com.sap.ec.sample

import android.util.Log
import com.sap.ec.AndroidEngagementCloud
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.api.event.model.AppEvent
import com.sap.ec.api.event.model.BadgeCountEvent
import com.sap.ec.config.LinkContactData

actual suspend fun enableTracking() {
    AndroidEngagementCloud.setup.enable(
        AndroidEngagementCloudSDKConfig("EMSE3-B4341"),
//        AndroidEngagementCloudSDKConfig("EMS7F-6F32D"), for appCode change test, staging AppCode
        onContactLinkingFailed = {
            LinkContactData.ContactFieldValueData("test1@test.com")
        }
    )
    AndroidEngagementCloud.events.collect {
        when (it) {
            is AppEvent -> Log.i(
                "Engagement Cloud SDK",
                "Received AppEvent: ${it.name} with payload: ${it.payload}"
            )

            is BadgeCountEvent -> Log.i(
                "Engagement Cloud SDK",
                "Received BadgeCountEvent with method: ${it.method} and count: ${it.badgeCount}"
            )
        }
    }
}