package com.emarsys.di

import com.emarsys.api.config.Config
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.config.ConfigCall
import com.emarsys.api.config.ConfigContext
import com.emarsys.api.config.ConfigContextApi
import com.emarsys.api.config.ConfigInstance
import com.emarsys.api.config.ConfigInternal
import com.emarsys.api.config.GathererConfig
import com.emarsys.api.config.LoggingConfig
import com.emarsys.core.collections.PersistentList
import com.emarsys.core.language.LanguageHandler
import com.emarsys.core.language.LanguageHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object ConfigInjection {
    val configModules = module {
        single<MutableList<ConfigCall>>(named(PersistentListTypes.ConfigCall)) {
            PersistentList(
                id = PersistentListIds.CONFIG_CONTEXT_PERSISTENT_ID,
                storage = get(),
                elementSerializer = ConfigCall.serializer(),
                elements = listOf()
            )
        }
        single<ConfigContextApi> { ConfigContext(
            calls = get(named(PersistentListTypes.ConfigCall))
        ) }
        single<ConfigInstance>(named(InstanceType.Logging)) {
            LoggingConfig(
                logger = get { parametersOf(LoggingConfig::class.simpleName) },
            )
        }
        single<ConfigInstance>(named(InstanceType.Gatherer)) {
            GathererConfig(
                configContext = get(),
                sdkLogger = get { parametersOf(GathererConfig::class.simpleName) },
            )
        }
        single<ConfigInstance>(named(InstanceType.Internal)) {
            ConfigInternal(
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
                uuidProvider = get(),
                timestampProvider = get(),
                sdkLogger = get { parametersOf(ConfigInternal::class.simpleName) },
                languageHandler = get()
            )
        }
        single<LanguageHandlerApi> {
            LanguageHandler(
                stringStorage = get(),
                languageTagValidator = get(),
                sdkEvents = get(named(EventFlowTypes.InternalEventFlow)),
                logger = get { parametersOf(LanguageHandler::class.simpleName) }
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