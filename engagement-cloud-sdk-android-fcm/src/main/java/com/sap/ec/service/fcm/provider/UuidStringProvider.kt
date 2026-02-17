package com.sap.ec.service.fcm.provider

import java.util.UUID

class UuidStringProvider {

    fun provide(): String {
        return UUID.randomUUID().toString()
    }
}