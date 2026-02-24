package com.sap.ec.sample

import JSEngagementCloud
import com.sap.ec.JsApiConfig
import com.sap.ec.api.setup.JsContactFieldValueData
import kotlin.js.Promise

actual suspend fun enableTracking() {
    JSEngagementCloud.setup.enable(JsApiConfig("EMSE3-B4341"), onContactLinkingFailed = {
        Promise.resolve(
            js("{contactFieldValue: 'test@test.com'}").unsafeCast<JsContactFieldValueData>())
    })
}