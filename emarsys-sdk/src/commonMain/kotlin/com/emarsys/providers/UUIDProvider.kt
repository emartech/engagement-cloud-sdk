package com.emarsys.providers

import com.benasher44.uuid.uuid4

class UUIDProvider: Provider<String> {
    override fun provide(): String {
        return uuid4().toString()
    }

}