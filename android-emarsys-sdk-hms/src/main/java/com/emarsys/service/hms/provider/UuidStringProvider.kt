package com.emarsys.service.hms.provider

import java.util.UUID

class UuidStringProvider {

    fun provide(): String {
        return UUID.randomUUID().toString()
    }
}