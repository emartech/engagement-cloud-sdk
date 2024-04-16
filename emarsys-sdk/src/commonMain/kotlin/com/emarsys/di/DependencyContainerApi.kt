package com.emarsys.di

import EventTrackerApi
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactInternalApi
import com.emarsys.api.event.EventTrackerInternalApi
import com.emarsys.api.geofence.GeofenceApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.oneventaction.OnEventActionApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.core.providers.Provider
import com.emarsys.mobileengage.session.Session
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

interface DependencyContainerApi {
    val contactApi: ContactInternalApi

    val eventTrackerApi: EventTrackerApi

    val inAppApi: InAppApi

    val inboxApi: InboxApi

    val predictApi: PredictApi

    val pushApi: PushApi

    val geofenceApi: GeofenceApi

    val configApi: ConfigApi

    val onEventActionApi: OnEventActionApi

    val uuidProvider: Provider<String>

    val timezoneProvider: Provider<String>

    val setupOrganizerApi: SetupOrganizerApi

    val remoteConfigHandler: RemoteConfigHandlerApi

    val connectionWatchDog: ConnectionWatchDog

    val lifecycleWatchDog: LifecycleWatchDog

    val mobileEngageSession: Session

    suspend fun setup()
}
