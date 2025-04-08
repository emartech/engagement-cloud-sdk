package com.emarsys.setup.states

import com.emarsys.core.state.State
import com.emarsys.networking.clients.EventBasedClientApi

internal class RegisterEventBasedClientsState(private val clients: List<EventBasedClientApi>) :
    State {
    override val name: String
        get() = "registerMobileEngageClients"

    override fun prepare() {}

    override suspend fun active() {
        clients.forEach {
            it.register()
        }
    }

    override fun relax() {}
}