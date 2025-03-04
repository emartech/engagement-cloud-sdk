package com.emarsys.di

import EventTrackerApi
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.init.InitOrganizerApi

import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.setup.SetupOrganizerApi
import kotlinx.coroutines.flow.SharedFlow

interface DependencyContainerApi {
    val contactApi: ContactApi

    val eventTrackerApi: EventTrackerApi

    val inAppApi: InAppApi

    val inboxApi: InboxApi

    val predictApi: PredictApi

    val pushApi: PushApi

    val geofenceTrackerApi: GeofenceTrackerApi

    val configApi: ConfigApi

    val deepLinkApi: DeepLinkApi

    val setupOrganizerApi: SetupOrganizerApi

    val initOrganizer: InitOrganizerApi

    val events: SharedFlow<SdkEvent>

    suspend fun setup()
}
