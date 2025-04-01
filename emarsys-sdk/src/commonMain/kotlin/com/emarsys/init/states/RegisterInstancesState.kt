package com.emarsys.init.states

import com.emarsys.api.contact.ContactApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.State
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.networking.clients.config.ConfigClient
import com.emarsys.networking.clients.contact.ContactClient
import com.emarsys.networking.clients.deepLink.DeepLinkClient

internal class RegisterInstancesState(
    private val eventTrackerApi: EventTrackerApi,
    private val contactApi: ContactApi,
    private val pushApi: PushApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "registerInstanceState"

    override fun prepare() {
    }

    override suspend fun active() {
        koin.get<ContactClient>()
        koin.get<ConfigClient>()
        koin.get<DeepLinkClient>()
        sdkLogger.debug("RegisterInstancesState", "Registering instances")
        eventTrackerApi.registerOnContext()
        contactApi.registerOnContext()
        pushApi.registerOnContext()
    }

    override fun relax() {
    }
}