package com.sap.ec

import androidx.compose.ui.window.ComposeUIViewController
import com.sap.ec.api.config.IosConfigApi
import com.sap.ec.api.contact.IosContactApi
import com.sap.ec.api.deeplink.IosDeepLinkApi
import com.sap.ec.api.embeddedmessaging.IosEmbeddedMessagingApi
import com.sap.ec.api.inapp.IosInAppApi
import com.sap.ec.api.push.IosPushApi
import com.sap.ec.api.setup.IosSetupApi
import com.sap.ec.api.tracking.IosTrackingApi
import com.sap.ec.di.CoroutineScopeTypes
import com.sap.ec.di.EventFlowTypes
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.view.InlineInAppView
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

typealias EngagementCloudEventListener = (SdkEvent) -> Unit

@OptIn(ExperimentalObjCName::class)
@ObjCName("EngagementCloud")
object IosEngagementCloud {
    init {
        SdkKoinIsolationContext.init()
    }

    private var eventListeners: MutableList<EngagementCloudEventListener> = mutableListOf()

    val setup: IosSetupApi
        get() = koin.get<IosSetupApi>()
    val contact: IosContactApi
        get() = koin.get<IosContactApi>()
    val push: IosPushApi
        get() = koin.get<IosPushApi>()
    val event: IosTrackingApi
        get() = koin.get<IosTrackingApi>()
    val inApp: IosInAppApi
        get() = koin.get<IosInAppApi>()
    val config: IosConfigApi
        get() = koin.get<IosConfigApi>()
    val deepLink: IosDeepLinkApi
        get() = koin.get<IosDeepLinkApi>()
    val embeddedMessaging: IosEmbeddedMessagingApi
        get() = koin.get<IosEmbeddedMessagingApi>()

    /**
     * Initializes the SDK. This method must be called before using any other SDK functionality.
     */
    @Throws(CancellationException::class)
    suspend fun initialize() {
        EngagementCloud.initialize()
        koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
            .launch(start = CoroutineStart.UNDISPATCHED) {
                koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public)).collect {
                    eventListeners.forEach { listener ->
                        listener.invoke(it)
                    }
                }
            }
    }

    /**
     * Registers an event listener to receive SDK events.
     */
    fun registerEventListener(listener: EngagementCloudEventListener) {
        eventListeners.add(listener)
    }
}