package com.sap.ec.di

import com.sap.ec.api.inapp.GathererInApp
import com.sap.ec.api.inapp.InApp
import com.sap.ec.api.inapp.InAppApi
import com.sap.ec.api.inapp.InAppCall
import com.sap.ec.api.inapp.InAppConfig
import com.sap.ec.api.inapp.InAppConfigApi
import com.sap.ec.api.inapp.InAppInstance
import com.sap.ec.api.inapp.InAppInternal
import com.sap.ec.api.inapp.LoggingInApp
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacer
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.jsbridge.JsBridgeVerifier
import com.sap.ec.mobileengage.inapp.jsbridge.JsBridgeVerifierApi
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloader
import com.sap.ec.mobileengage.inapp.networking.download.InAppDownloaderApi
import com.sap.ec.mobileengage.inapp.networking.download.InlineInAppMessageFetcher
import com.sap.ec.mobileengage.inapp.networking.download.InlineInAppMessageFetcherApi
import com.sap.ec.mobileengage.inapp.presentation.InAppEventConsumer
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object InAppInjection {
    val inAppModules = module {
        single<JsBridgeVerifierApi> {
            JsBridgeVerifier(
                stringStorage = get(),
                jsBridgeClient = get(),
                sdkLogger = get { parametersOf(JsBridgeVerifier::class.simpleName) }
            )
        }
        single<ContentReplacerApi> {
            ContentReplacer(
                sdkVersionProvider = get(),
                jsBridgeVerifier = get(),
                stringStorage = get()
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
                genericClient = get(named(NetworkClientTypes.Generic)),
                networkClient = get(named(NetworkClientTypes.EC)),
                urlFactory = get(),
                json = get(),
                sdkLogger = get { parametersOf(InlineInAppMessageFetcher::class.simpleName) }
            )
        }
        single<ThreadSafePersistentStoreApi<InAppCall>>(named(ThreadSafePersistentStoreTypes.InAppCall)) {
            ThreadSafePersistentStore(
                id = PersistentStoreIds.INAPP_CONTEXT_PERSISTENT_ID,
                storage = get(),
                itemSerializer = InAppCall.serializer(),
            )
        }
        single<InAppEventConsumer> {
            InAppEventConsumer(
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkEventManager = get(),
                sdkLogger = get { parametersOf(InAppEventConsumer::class.simpleName) },
                inAppPresenter = get(),
                inAppViewProvider = get()
            )
        }
        single<InAppConfigApi> { InAppConfig() }
        single<InAppInstance>(named(InstanceType.Logging)) {
            LoggingInApp(
                sdkContext = get(),
                logger = get { parametersOf(LoggingInApp::class.simpleName) },
            )
        }
        single<InAppInstance>(named(InstanceType.Gatherer)) {
            GathererInApp(
                inAppConfig = get(),
                threadSafePersistentStore = get(named(ThreadSafePersistentStoreTypes.InAppCall)),
            )
        }
        single<InAppInstance>(named(InstanceType.Internal)) {
            InAppInternal(
                inAppConfig = get(),
                threadSafePersistentStore = get(named(ThreadSafePersistentStoreTypes.InAppCall))
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