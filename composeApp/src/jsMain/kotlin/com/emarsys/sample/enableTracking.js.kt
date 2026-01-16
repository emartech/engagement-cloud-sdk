package com.emarsys.sample

import EmarsysJs
import com.emarsys.JsApiConfig

actual suspend fun enableTracking() {
    EmarsysJs.setup.enableTracking(JsApiConfig("EMSE3-B4341"))
}