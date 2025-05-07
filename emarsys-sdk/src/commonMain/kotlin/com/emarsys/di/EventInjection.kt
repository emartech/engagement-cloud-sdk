package com.emarsys.di

import com.emarsys.api.event.EventTracker
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.event.EventTrackerCall
import com.emarsys.api.event.EventTrackerContext
import com.emarsys.api.event.EventTrackerContextApi
import com.emarsys.api.event.EventTrackerGatherer
import com.emarsys.api.event.EventTrackerInstance
import com.emarsys.api.event.EventTrackerInternal
import com.emarsys.api.event.LoggingEventTracker
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.PersistentList
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.Session
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.tracking.Tracking
import com.emarsys.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object EventInjection {
    val eventModules = module {
        single<MutableList<EventTrackerCall>>(named(PersistentListTypes.EventTrackerCall)) {
            PersistentList(
                id = PersistentListIds.EVENT_TRACKER_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = EventTrackerCall.serializer(),
                elements = listOf()
            )
        }
        single<EventTrackerContextApi> {
            EventTrackerContext(
                calls = get(named(PersistentListTypes.EventTrackerCall))
            )
        }
        single<EventTrackerInstance>(named(InstanceType.Logging)) {
            LoggingEventTracker(
                logger = get { parametersOf(LoggingEventTracker::class.simpleName) },
            )
        }
        single<EventTrackerInstance>(named(InstanceType.Gatherer)) {
            EventTrackerGatherer(
                context = get(),
                timestampProvider = get(),
                uuidProvider = get(),
                sdkLogger = get { parametersOf(EventTrackerGatherer::class.simpleName) },
            )
        }
        single<EventTrackerInstance>(named(InstanceType.Internal)) {
            EventTrackerInternal(
                sdkEventDistributor = get(),
                eventTrackerContext = get(),
                timestampProvider = get(),
                uuidProvider = get(),
                sdkLogger = get { parametersOf(EventTrackerInternal::class.simpleName) },
            )
        }
        single<EventTrackerApi> {
            EventTracker(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
        single<Session> {
            MobileEngageSession(
                timestampProvider = get(),
                uuidProvider = get(),
                sessionContext = get(),
                sdkContext = get(),
                sdkEventDistributor = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkLogger = get { parametersOf(MobileEngageSession::class.simpleName) },
            )
        }
        single<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public)) {
            get<SdkEventDistributorApi>().sdkEventFlow.filterIsInstance<SdkEvent.External.Api>()
        }
        single<TrackingApi> { Tracking() }
    }
}