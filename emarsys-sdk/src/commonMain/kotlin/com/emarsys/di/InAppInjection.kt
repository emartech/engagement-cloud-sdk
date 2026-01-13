package com.emarsys.di

import com.emarsys.api.inapp.GathererInApp
import com.emarsys.api.inapp.InApp
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inapp.InAppCall
import com.emarsys.api.inapp.InAppConfig
import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.api.inapp.InAppContext
import com.emarsys.api.inapp.InAppContextApi
import com.emarsys.api.inapp.InAppInstance
import com.emarsys.api.inapp.InAppInternal
import com.emarsys.api.inapp.LoggingInApp
import com.emarsys.core.collections.PersistentList
import com.emarsys.mobileengage.inapp.InAppDownloader
import com.emarsys.mobileengage.inapp.InAppDownloaderApi
import com.emarsys.mobileengage.inapp.InAppEventConsumer
import com.emarsys.mobileengage.inapp.InlineInAppMessageFetcher
import com.emarsys.mobileengage.inapp.InlineInAppMessageFetcherApi
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object InAppInjection {
    val inAppModules = module {
        single<InAppDownloaderApi> {
            InAppDownloader(
                emarsysClient = get(named(NetworkClientTypes.Emarsys)),
                sdkLogger = get { parametersOf(InAppDownloader::class.simpleName) }
            )
        }
        single<InlineInAppMessageFetcherApi> {
            InlineInAppMessageFetcher(
                networkClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                json = get()
            )
        }
        single<MutableList<InAppCall>>(named(PersistentListTypes.InAppCall)) {
            PersistentList(
                id = PersistentListIds.INAPP_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = InAppCall.serializer(),
                elements = listOf()
            )
        }
        single<InAppEventConsumer>{
            InAppEventConsumer(
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkEventManager = get(),
                sdkLogger = get { parametersOf(InAppEventConsumer::class.simpleName) },
                inAppPresenter = get(),
                inAppViewProvider = get()
            )
        }
        single<InAppConfigApi> { InAppConfig() }
        single<InAppContextApi> {
            InAppContext(
                calls = get(named(PersistentListTypes.InAppCall))
            )
        }
        single<InAppInstance>(named(InstanceType.Logging)) {
            LoggingInApp(
                sdkContext = get(),
                logger = get { parametersOf(LoggingInApp::class.simpleName) },
            )
        }
        single<InAppInstance>(named(InstanceType.Gatherer)) {
            GathererInApp(
                inAppConfig = get(),
                inAppContext = get(),
            )
        }
        single<InAppInstance>(named(InstanceType.Internal)) {
            InAppInternal(
                inAppConfig = get(),
                inAppContext = get()
            )
        }
        single<InAppApi> {
            InApp(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}