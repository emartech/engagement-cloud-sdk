package com.emarsys.sample

import JsEmarsysConfig

actual suspend fun enableTracking() {
    EmarsysJs.setup.enableTracking(JsEmarsysConfig("EMSE3-B4341"))
}