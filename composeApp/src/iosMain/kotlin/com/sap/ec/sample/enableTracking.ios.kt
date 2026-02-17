package com.sap.ec.sample

import com.sap.ec.IosEngagementCloud
import com.sap.ec.IosEngagementCloudSDKConfig

actual suspend fun enableTracking() {
    IosEngagementCloud.setup.enableTracking(IosEngagementCloudSDKConfig("EMSE3-B4341"))
}