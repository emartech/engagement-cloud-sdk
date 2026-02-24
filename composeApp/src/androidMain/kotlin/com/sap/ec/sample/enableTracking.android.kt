package com.sap.ec.sample

import com.sap.ec.AndroidEngagementCloud
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData

actual suspend fun enableTracking() {
    AndroidEngagementCloud.setup.enable(
        AndroidEngagementCloudSDKConfig("EMSE3-B4341"),
        onContactLinkingFailed = {
            LinkContactData.ContactFieldValueData("test@test.com")
        }
    )
}