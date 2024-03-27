package com.emarsys.mobileengage.session

import com.emarsys.api.SdkResult
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext

class MobileEngageSession(
    private val timestampProvider: Provider<Long>,
    private val uuidProvider: Provider<String>,
    private val sessionContext: SessionContext,
    private val eventClient: NetworkClientApi
) : Session {
    private var sessionStart: Long? = null
    override suspend fun startSession(): SdkResult {
        TODO("Not yet implemented")
    }

    override suspend fun endSession(): SdkResult {
        TODO("Not yet implemented")
    }
}