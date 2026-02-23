package com.sap.ec.sample

import JSEngagementCloud
import com.sap.ec.JsApiConfig

actual suspend fun enableTracking() {
    JSEngagementCloud.setup.enable(JsApiConfig("EMSE3-B4341"))
}