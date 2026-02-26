package com.sap.ec.sample

import com.sap.ec.IosEngagementCloud
import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData

actual suspend fun enableTracking() {
    IosEngagementCloud.setup.enable(
        IosEngagementCloudSDKConfig("EMSE3-B4341"),
        onContactLinkingFailed = { onSuccess, onError ->
            LinkContactData.ContactFieldValueData("test@test.com")
        })
}