package com.sap.ec.enable.states

import com.sap.ec.core.state.State
import com.sap.ec.networking.clients.EventBasedClientApi

internal class RegisterEventBasedClientsState(private val clients: List<EventBasedClientApi>) :
    State {
    override val name: String
        get() = "registerMobileEngageClients"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        clients.forEach {
            it.register()
        }
        return Result.success(Unit)
    }

    override fun relax() {}
}