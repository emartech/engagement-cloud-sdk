package com.emarsys.di

import com.emarsys.api.contact.ContactApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.push.PushApi
import com.emarsys.providers.Provider
import com.emarsys.setup.SetupOrganizerApi

interface DependencyContainerApi {
    val contactApi: ContactApi

    val eventTrackerApi: EventTrackerApi

    val pushApi: PushApi

    val uuidProvider: Provider<String>

    val setupOrganizerApi: SetupOrganizerApi

}
