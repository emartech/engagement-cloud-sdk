package com.sap.ec.core.providers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
internal class UUIDProvider: UuidProviderApi {
    override fun provide(): String {
        return Uuid.random().toString()
    }
}