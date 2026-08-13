package com.sap.ec.di

import com.sap.ec.api.config.Config
import com.sap.ec.api.config.ConfigApi
import com.sap.ec.api.config.ConfigCall
import com.sap.ec.api.config.ConfigInstance
import com.sap.ec.api.config.ConfigInternal
import com.sap.ec.api.config.GathererConfig
import com.sap.ec.api.config.LoggingConfig
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.language.LanguageHandler
import com.sap.ec.core.language.LanguageHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal object ConfigInjection {
    val configModules = module {
        single<ThreadSafePersistentStoreApi<ConfigCall>>(named(ThreadSafePersistentStoreTypes.ConfigCall)) {
            ThreadSafePersistentStore(
                id = PersistentListIds.CONFIG_CONTEXT_PERSISTENT_ID,
                storage = get(),
                itemSerializer = ConfigCall.serializer()
            )
        }
        single<ConfigInstance>(named(InstanceType.Logging)) {
            LoggingConfig(
                logger = get { parametersOf(LoggingConfig::class.simpleName) },
            )
        }
        single<ConfigInstance>(named(InstanceType.Gatherer)) {
            GathererConfig(
                threadSafePersistentStore = get(named(ThreadSafePersistentStoreTypes.ConfigCall)),
                sdkLogger = get { parametersOf(GathererConfig::class.simpleName) },
            )
        }
        single<ConfigInstance>(named(InstanceType.Internal)) {
            ConfigInternal(
                sdkEventDistributor = get(),
                threadSafePersistentStore = get(named(ThreadSafePersistentStoreTypes.ConfigCall)),
                uuidProvider = get(),
                timestampProvider = get(),
                sdkLogger = get { parametersOf(ConfigInternal::class.simpleName) },
                languageHandler = get(),
                sdkContext = get()
            )
        }
        single<LanguageHandlerApi> {
            LanguageHandler(
                stringStorage = get(),
                languageTagValidator = get(),
                sdkEventManager = get(),
                logger = get { parametersOf(LanguageHandler::class.simpleName) },
                sdkContext = get()
            )
        }
        single<ConfigApi> {
            Config(
                loggingApi = get(named(InstanceType.Logging)),
                gathererApi = get(named(InstanceType.Gatherer)),
                internalApi = get(named(InstanceType.Internal)),
                deviceInfoCollector = get(),
                sdkContext = get()
            )
        }
    }
}