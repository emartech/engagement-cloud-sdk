package com.sap.ec.sample

import com.sap.ec.IosEmarsysConfig
import com.sap.ec.IosEngagementCloud

actual suspend fun enableTracking() {
    IosEngagementCloud.setup.enableTracking(IosEmarsysConfig("EMSE3-B4341"))
}