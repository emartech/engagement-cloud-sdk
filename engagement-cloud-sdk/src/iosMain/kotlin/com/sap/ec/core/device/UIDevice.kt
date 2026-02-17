package com.sap.ec.core.device

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo

class UIDevice(
    private val processInfo: NSProcessInfo
) : UIDeviceApi {
    override fun osVersion(): String {
        return platform.UIKit.UIDevice.currentDevice.systemVersion
    }

    override fun deviceModel(): String {
        return platform.UIKit.UIDevice.currentDevice.model
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun hasOsVersionAtLeast(majorVersion: Int): Boolean {
        val systemVersion = cValue<NSOperatingSystemVersion> {
            this.majorVersion = majorVersion.toLong()
            minorVersion = 0
            patchVersion = 0
        }
        return processInfo.isOperatingSystemAtLeastVersion(systemVersion)
    }


}