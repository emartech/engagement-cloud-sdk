package com.sap.ec.sample

import JSEngagementCloud
import com.sap.ec.JsApiConfig

actual suspend fun enableTracking() {
    JSEngagementCloud.setup.enable(JsApiConfig("EMS11-C3FD3"))
}