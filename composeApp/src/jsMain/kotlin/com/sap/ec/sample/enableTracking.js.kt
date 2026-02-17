package com.sap.ec.sample

import JSEngagementCloud
import com.sap.ec.JsApiConfig

actual suspend fun enableTracking() {
    JSEngagementCloud.setup.enableTracking(JsApiConfig("EMS11-C3FD3"))
}