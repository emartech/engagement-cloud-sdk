package com.emarsys.di

import android.app.NotificationManager
import com.emarsys.applicationContext
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.push.AndroidPushMessageFactory
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.mobileengage.push.mapper.AndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

class AndroidPlatformContext(
    private val json: Json,
    private val pushActionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
    uuidProvider: Provider<String>,
    sdkLogger: Logger,
    notificationManager: NotificationManager,
    metadataReader: MetadataReader,
    downloaderApi: DownloaderApi,
    platformInfoCollector: PlatformInfoCollector,
    inAppDownloader: InAppDownloaderApi,
    sdkEventFlow: MutableSharedFlow<SdkEvent>
) : PlatformContext {

    val notificationIntentProcessor: NotificationIntentProcessor by lazy {
        NotificationIntentProcessor(json, pushActionFactory, actionHandler, sdkLogger)
    }

    val silentPushHandler = SilentPushMessageHandler(pushActionFactory, sdkEventFlow)

    val pushMessagePresenter = PushMessagePresenter(
        applicationContext,
        json,
        notificationManager,
        metadataReader,
        NotificationCompatStyler(downloaderApi),
        platformInfoCollector,
        inAppDownloader,
        sdkLogger
    )

    val androidPushMessageFactory = AndroidPushMessageFactory(
        androidPushV1Mapper = AndroidPushV1Mapper(sdkLogger, json, uuidProvider),
        silentAndroidPushV1Mapper = SilentAndroidPushV1Mapper(sdkLogger, json),
        androidPushV2Mapper = AndroidPushV2Mapper(uuidProvider, sdkLogger, json),
        silentAndroidPushV2Mapper = SilentAndroidPushV2Mapper(sdkLogger, json)
    )
}