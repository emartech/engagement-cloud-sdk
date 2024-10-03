package com.emarsys.core.providers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class UUIDProvider: Provider<String> {
    override fun provide(): String {
        return Uuid.random().toString()
    }

}