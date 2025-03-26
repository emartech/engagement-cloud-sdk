package com.emarsys.core.networking

import com.emarsys.core.providers.SuspendProvider

internal interface UserAgentProviderApi: SuspendProvider<String> {
    override suspend fun provide(): String
}