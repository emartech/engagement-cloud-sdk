package com.emarsys.sample

import EmarsysJs
import com.emarsys.JsApiConfig

actual suspend fun enableTracking() {
    EmarsysJs.setup.enableTracking(JsApiConfig("EMS11-C3FD3"))
}