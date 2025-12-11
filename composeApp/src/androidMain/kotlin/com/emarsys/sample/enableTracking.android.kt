package com.emarsys.sample

import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig

actual suspend fun enableTracking() {
    AndroidEmarsys.setup.enableTracking(AndroidEmarsysConfig("EMS11-C3FD3"))
}