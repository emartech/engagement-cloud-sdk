package com.emarsys.di

import android.app.NotificationManager
import com.emarsys.applicationContext
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.resource.MetadataReader
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.push.NotificationCompatStyler
import com.emarsys.mobileengage.push.NotificationIntentProcessor
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.SilentPushMessageHandler
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json

class AndroidPlatformContext(
    private val json: Json,
    private val pushActionFactory: ActionFactoryApi<ActionModel>,
    private val actionHandler: ActionHandlerApi,
    notificationManager: NotificationManager,
    metadataReader: MetadataReader,
    downloaderApi: DownloaderApi,
    platformInfoCollector: PlatformInfoCollector,
    inAppDownloader: InAppDownloaderApi,
    sdkEventFlow: MutableSharedFlow<SdkEvent>
) : PlatformContext {

    val notificationIntentProcessor: NotificationIntentProcessor by lazy {
        NotificationIntentProcessor(json, pushActionFactory, actionHandler)
    }

    val silentPushHandler = SilentPushMessageHandler(pushActionFactory, sdkEventFlow)

    val pushMessagePresenter = PushMessagePresenter(
        applicationContext,
        json,
        notificationManager,
        metadataReader,
        NotificationCompatStyler(downloaderApi),
        platformInfoCollector,
        inAppDownloader
    )
}