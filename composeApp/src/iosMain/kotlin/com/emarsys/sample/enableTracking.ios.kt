package com.emarsys.sample

import com.emarsys.IosEmarsys
import com.emarsys.IosEmarsysConfig

actual suspend fun enableTracking() {
    IosEmarsys.setup.enableTracking(IosEmarsysConfig("EMSE3-B4341"))
}