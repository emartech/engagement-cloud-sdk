package com.sap.ec.di

import com.sap.ec.api.config.Config
import com.sap.ec.api.config.ConfigApi
import com.sap.ec.api.config.ConfigCall
import com.sap.ec.api.config.ConfigContext
import com.sap.ec.api.config.ConfigContextApi
import com.sap.ec.api.config.ConfigInstance
import com.sap.ec.api.config.ConfigInternal
import com.sap.ec.api.config.GathererConfig
import com.sap.ec.api.config.LoggingConfig
import com.sap.ec.core.collections.PersistentList
import com.sap.ec.core.language.LanguageHandler
import com.sap.ec.core.language.LanguageHandlerApi
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
                sdkEventDistributor = get(),
                configContext = get(),
                uuidProvider = get(),
                timestampProvider = get(),
                sdkLogger = get { parametersOf(ConfigInternal::class.simpleName) },
                languageHandler = get(),
            )
        }
        single<LanguageHandlerApi> {
            LanguageHandler(
                stringStorage = get(),
                languageTagValidator = get(),
                sdkEventDistributor = get(),
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