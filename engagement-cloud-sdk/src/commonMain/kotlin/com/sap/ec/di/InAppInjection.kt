package com.sap.ec.di

import com.sap.ec.api.inapp.GathererInApp
import com.sap.ec.api.inapp.InApp
import com.sap.ec.api.inapp.InAppApi
import com.sap.ec.api.inapp.InAppCall
import com.sap.ec.api.inapp.InAppConfig
import com.sap.ec.api.inapp.InAppConfigApi
import com.sap.ec.api.inapp.InAppContext
import com.sap.ec.api.inapp.InAppContextApi
import com.sap.ec.api.inapp.InAppInstance
import com.sap.ec.api.inapp.InAppInternal
import com.sap.ec.api.inapp.LoggingInApp
import com.sap.ec.core.collections.PersistentList
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacer
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloader
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloaderApi
import com.sap.ec.mobileengage.inapp.networking.download.InlineInAppMessageFetcher
import com.sap.ec.mobileengage.inapp.networking.download.InlineInAppMessageFetcherApi
import com.sap.ec.mobileengage.inapp.presentation.InAppEventConsumer
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object InAppInjection {
    val inAppModules = module {
        single<ContentReplacerApi> {
            ContentReplacer(
                sdkContext = get(),
                sdkVersionProvider = get()
            )
        }
        single<InAppDownloaderApi> {
            InAppDownloader(
                ecClient = get(named(NetworkClientTypes.EC)),
                sdkLogger = get { parametersOf(InAppDownloader::class.simpleName) }
            )
        }
        single<InlineInAppMessageFetcherApi> {
            InlineInAppMessageFetcher(
                networkClient = get(named(NetworkClientTypes.EC)),
                urlFactory = get(),
                json = get(),
                sdkLogger = get { parametersOf(InlineInAppMessageFetcher::class.simpleName) }
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