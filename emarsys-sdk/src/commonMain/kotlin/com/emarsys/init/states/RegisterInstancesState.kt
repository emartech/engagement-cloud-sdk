package com.emarsys.init.states

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.embeddedmessaging.EmbeddedMessagingApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State

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