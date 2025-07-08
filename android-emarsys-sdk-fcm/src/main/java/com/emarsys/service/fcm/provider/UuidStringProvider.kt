package com.emarsys.service.fcm.provider

import java.util.UUID

class UuidStringProvider {

    fun provide(): String {
        return UUID.randomUUID().toString()
    }
}