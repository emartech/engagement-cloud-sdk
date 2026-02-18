package com.sap.ec.sample

import com.sap.ec.AndroidEngagementCloud
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig

actual suspend fun enableTracking() {
    AndroidEngagementCloud.setup.enableTracking(AndroidEngagementCloudSDKConfig("INS-S01-APP-ABC12"))
}