package com.emarsys.di

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.geofence.GeofenceApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.oneventaction.OnEventActionApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.providers.Provider
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import com.emarsys.setup.SetupOrganizerApi

interface DependencyContainerApi {
    val contactApi: ContactApi

    val eventTrackerApi: EventTrackerApi

    val inAppApi: InAppApi

    val inbox: InboxApi

    val predict: PredictApi

    val pushApi: PushApi

    val geofence: GeofenceApi

    val config: ConfigApi

    val onEventAction: OnEventActionApi

    val uuidProvider: Provider<String>

    val timezoneProvider: Provider<String>

    val setupOrganizerApi: SetupOrganizerApi
    
    val remoteConfigHandler: RemoteConfigHandlerApi
}
