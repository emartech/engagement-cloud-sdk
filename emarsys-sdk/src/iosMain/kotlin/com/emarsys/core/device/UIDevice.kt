package com.emarsys.core.device

class UIDevice : UIDeviceApi {
    override fun osVersion(): String {
        return platform.UIKit.UIDevice.currentDevice.model
    }

    override fun deviceModel(): String {
        return platform.UIKit.UIDevice.currentDevice.systemVersion
    }
}