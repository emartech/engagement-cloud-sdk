package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.language.LanguageTagValidatorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.state.State
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppHandlerApi
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object PlatformInjection {
    val platformModules = module {
        single<EventsDaoApi> { get<DependencyCreator>().createEventsDao() }
        single<DeviceInfoCollectorApi> {
            get<DependencyCreator>().createDeviceInfoCollector(
                timezoneProvider = get(),
                typedStorage = get()
            )
        }
        single<State> {
            get<DependencyCreator>().createPlatformInitState(
                pushApi = get(),
                sdkDispatcher = get(),
                sdkContext = get(),
                actionFactory = get(),
                storage = get()
            )
        }
        single<PermissionHandlerApi> { get<DependencyCreator>().createPermissionHandler() }
        single<ExternalUrlOpenerApi> { get<DependencyCreator>().createExternalUrlOpener() }
        single<ClipboardHandlerApi> { get<DependencyCreator>().createClipboardHandler() }
        single<LaunchApplicationHandlerApi> { get<DependencyCreator>().createLaunchApplicationHandler() }
        single<PushToInAppHandlerApi> {
            get<DependencyCreator>().createPushToInAppHandler(
                get<InAppDownloaderApi>(),
                get<InAppHandlerApi>()
            )
        }
        single<ConnectionWatchDog> {
            val logger: Logger = get { parametersOf(ConnectionWatchDog::class.simpleName) }
            get<DependencyCreator>().createConnectionWatchDog(logger)
        }
        single<LifecycleWatchDog> { get<DependencyCreator>().createLifeCycleWatchDog() }
        single<ApplicationVersionProviderApi> { get<DependencyCreator>().createApplicationVersionProvider() }
        single<LanguageProviderApi> { get<DependencyCreator>().createLanguageProvider() }
        single<LanguageTagValidatorApi> { get<DependencyCreator>().createLanguageTagValidator() }
        single<FileCacheApi> { get<DependencyCreator>().createFileCache() }
        single<InAppViewProviderApi> {
            get<DependencyCreator>().createInAppViewProvider(eventActionFactory = get())
        }
        single<InAppPresenterApi> { get<DependencyCreator>().createInAppPresenter() }
        single<PushApi> {
            get<DependencyCreator>().createPushApi(
                pushInternal = get(named(InstanceType.Internal)),
                get(),
                get()
            )
        }
    }
}
