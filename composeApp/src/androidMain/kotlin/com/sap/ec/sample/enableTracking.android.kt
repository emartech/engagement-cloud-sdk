package com.sap.ec.sample

import android.util.Log
import com.sap.ec.AndroidEngagementCloud
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData
import com.sap.ec.event.SdkEvent

actual suspend fun enableTracking() {
    AndroidEngagementCloud.setup.enable(
        AndroidEngagementCloudSDKConfig("EMSE3-B4341"),
        onContactLinkingFailed = {
            LinkContactData.ContactFieldValueData("test1@test.com")
        }
    )
    AndroidEngagementCloud.events.collect {
        val appEvent = it as SdkEvent.External.Api.AppEvent
        Log.d("AppEvent", "AppEvent: ${appEvent.name}}")
    }
}