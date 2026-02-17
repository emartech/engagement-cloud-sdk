package com.sap.ec.core.networking

import com.sap.ec.core.providers.SuspendProvider

internal interface UserAgentProviderApi: SuspendProvider<String> {
    override suspend fun provide(): String
}