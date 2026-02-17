package com.sap.ec.init.states

import com.sap.ec.api.config.ConfigApi
import com.sap.ec.api.contact.ContactApi
import com.sap.ec.api.embeddedmessaging.EmbeddedMessagingApi
import com.sap.ec.api.event.EventTrackerApi
import com.sap.ec.api.inapp.InAppApi
import com.sap.ec.api.push.PushApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State

internal class RegisterInstancesState(
    private val eventTrackerApi: EventTrackerApi,
    private val contactApi: ContactApi,
    private val configApi: ConfigApi,
    private val pushApi: PushApi,
    private val inAppApi: InAppApi,
    private val embeddedMessagingApi: EmbeddedMessagingApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "registerInstanceState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        sdkLogger.debug("Registering instances")
        eventTrackerApi.registerOnContext()
        contactApi.registerOnContext()
        configApi.registerOnContext()
        embeddedMessagingApi.registerOnContext()
        pushApi.registerOnContext()
        inAppApi.registerOnContext()

        return Result.success(Unit)
    }

    override fun relax() {
    }
}