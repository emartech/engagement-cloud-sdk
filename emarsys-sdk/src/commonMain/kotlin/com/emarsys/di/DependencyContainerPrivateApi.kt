package com.emarsys.di

import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.session.Session
import com.emarsys.networking.clients.contact.ContactClientApi
import com.emarsys.networking.clients.device.DeviceClientApi
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import kotlinx.serialization.json.Json

interface DependencyContainerPrivateApi {

    val pushActionFactory: ActionFactoryApi<ActionModel>

    val platformContext: PlatformContext

    val pushActionHandler: ActionHandlerApi

    val uuidProvider: Provider<String>

    val timezoneProvider: Provider<String>

    val remoteConfigHandler: RemoteConfigHandlerApi

    val connectionWatchDog: ConnectionWatchDog

    val lifecycleWatchDog: LifecycleWatchDog

    val mobileEngageSession: Session

    val json: Json

    val downloaderApi: DownloaderApi

    val inAppDownloader: InAppDownloaderApi

    val deviceClient: DeviceClientApi

    val pushClient: PushClientApi

    val contactClient: ContactClientApi

    val sessionContext: SessionContext

    val storage: Storage

    val stringStorage: TypedStorageApi<String?>

    val sdkContext: SdkContextApi
}
