package com.sap.ec.di

import com.sap.ec.api.event.EventTracker
import com.sap.ec.api.event.EventTrackerApi
import com.sap.ec.api.event.EventTrackerCall
import com.sap.ec.api.event.EventTrackerContext
import com.sap.ec.api.event.EventTrackerContextApi
import com.sap.ec.api.event.EventTrackerGatherer
import com.sap.ec.api.event.EventTrackerInstance
import com.sap.ec.api.event.EventTrackerInternal
import com.sap.ec.api.event.LoggingEventTracker
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.PersistentList
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.session.ECSdkSession
import com.sap.ec.mobileengage.session.SessionApi
import com.sap.ec.tracking.Tracking
import com.sap.ec.tracking.TrackingApi
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
        single<SessionApi> {
            ECSdkSession(
                timestampProvider = get(),
                uuidProvider = get(),
                requestContext = get(),
                sessionContext = get(),
                sdkContext = get(),
                sdkEventDistributor = get(),
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                sdkLogger = get { parametersOf(ECSdkSession::class.simpleName) })
        }
        single<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public)) {
            get<SdkEventDistributorApi>().sdkEventFlow.filterIsInstance<SdkEvent.External.Api>()
        }
        single<TrackingApi> { Tracking() }
    }
}