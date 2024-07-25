package com.emarsys.di

import EventTrackerApi
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.oneventaction.OnEventActionApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.setup.SetupOrganizerApi

interface DependencyContainerApi {
    val contactApi: ContactApi

    val eventTrackerApi: EventTrackerApi

    val inAppApi: InAppApi

    val inboxApi: InboxApi

    val predictApi: PredictApi

    val pushApi: PushApi

    val geofenceTrackerApi: GeofenceTrackerApi

    val configApi: ConfigApi

    val onEventActionApi: OnEventActionApi

    val setupOrganizerApi: SetupOrganizerApi

    suspend fun setup()
}
