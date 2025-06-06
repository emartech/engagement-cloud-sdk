package com.emarsys

import com.emarsys.api.config.ConfigApi
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.deeplink.DeepLinkApi
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.push.PushApi
import com.emarsys.config.SdkConfig
import com.emarsys.config.isValid
import com.emarsys.core.exceptions.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkAlreadyEnabledException
import com.emarsys.core.log.Logger
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.disable.DisableOrganizerApi
import com.emarsys.enable.EnableOrganizerApi
import com.emarsys.init.InitOrganizerApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.tracking.TrackingApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
@OptIn(ExperimentalObjCRefinement::class)
object Emarsys {

    suspend fun initialize() {
        SdkKoinIsolationContext.init()
        koin.get<InitOrganizerApi>().init()
    }

    suspend fun enableTracking(config: SdkConfig): Result<Unit> {
        val logger = koin.get<Logger> { parametersOf(Emarsys::class.simpleName) }
        config.isValid(logger)
        try {
            koin.get<EnableOrganizerApi>().enableWithValidation(config)
            return Result.success(Unit)
        } catch (exception: SdkAlreadyEnabledException) {
            return Result.failure(exception)
        }
    }

    suspend fun disableTracking(): Result<Unit> {
        try {
            koin.get<DisableOrganizerApi>().disable()
            return Result.success(Unit)
        } catch (exception: SdkAlreadyDisabledException) {
            return Result.failure(exception)
        }
    }

    val events: Flow<SdkEvent>
        get() = koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public))

    val contact: ContactApi
        get() = koin.get<ContactApi>()

    val push: PushApi
        get() = koin.get<PushApi>()

    val tracking: TrackingApi
        get() = koin.get<TrackingApi>()

    val inApp: InAppApi
        get() = koin.get<InAppApi>()

    val inbox: InboxApi
        get() = koin.get<InboxApi>()

    val config: ConfigApi
        get() = koin.get<ConfigApi>()

    val geofence: GeofenceTrackerApi
        get() = koin.get<GeofenceTrackerApi>()

    val predict: PredictApi
        get() = koin.get<PredictApi>()

    val deepLink: DeepLinkApi
        get() = koin.get<DeepLinkApi>()

}
