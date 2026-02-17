package com.sap.ec.iosNotificationService.provider

import platform.Foundation.NSUUID
import platform.Foundation.NSUUID.Companion.UUID

class UUIDProvider: Provider<NSUUID> {

    override fun provide(): NSUUID {
        return UUID()
    }
}
