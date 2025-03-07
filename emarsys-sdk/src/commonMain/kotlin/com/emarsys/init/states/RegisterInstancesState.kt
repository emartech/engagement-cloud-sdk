package com.emarsys.init.states

import EventTrackerApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State

class RegisterInstancesState(
    private val eventTrackerApi: EventTrackerApi,
    private val contactApi: ContactApi,
    private val pushApi: PushApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "registerInstanceState"

    override fun prepare() {
    }

    override suspend fun active() {
        sdkLogger.debug("RegisterInstancesState", "Registering instances")

        eventTrackerApi.registerOnContext()
        contactApi.registerOnContext()
        pushApi.registerOnContext()
    }

    override fun relax() {
    }
}