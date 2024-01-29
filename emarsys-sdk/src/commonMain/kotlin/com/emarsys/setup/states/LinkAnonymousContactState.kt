package com.emarsys.setup.states

import com.emarsys.core.state.State
import com.emarsys.networking.clients.contact.ContactClientApi
import com.emarsys.session.SessionContext

class LinkAnonymousContactState(
    private val contactClient: ContactClientApi,
    private val sessionContext: SessionContext
) : State {

    override val name: String = "linkAnonymousContactState"

    override fun prepare() {
    }

    override suspend fun active() {
        if (!sessionContext.hasContactIdentification()) {
            contactClient.unlinkContact()
        }
    }

    override fun relax() {
    }
}