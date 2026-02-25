package com.sap.ec.enable.states

import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.networking.clients.jsbridge.JsBridgeClientApi

internal class FetchJsBridgeState(
    private val jsBridgeClient: JsBridgeClientApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "fetchJsBridge"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("FetchJsBridgeState started")
        return jsBridgeClient.validateJSBridge()
    }

    override fun relax() {
    }
}
