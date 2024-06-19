package com.emarsys.service.provider

import java.util.UUID

class UuidStringProvider {

    fun provide(): String {
        return UUID.randomUUID().toString()
    }
}