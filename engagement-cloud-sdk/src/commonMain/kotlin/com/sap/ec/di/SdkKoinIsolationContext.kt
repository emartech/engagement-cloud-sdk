package com.sap.ec.di

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.init.InitOrganizerApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication

object SdkKoinIsolationContext {
    private var runningApp: KoinApplication? = null

    fun isInitialized(): Boolean = runningApp != null

    private val koinApp = koinApplication {
        modules(
            InitInjection.initModules,
            SetupInjection.setupModules,
            CoreInjection.coreModules,
            NetworkInjection.networkModules,
            RemoteConfigInjection.remoteConfigModules,
            ConfigInjection.configModules,
            ContactInjection.contactModules,
            EventInjection.eventModules,
            PushInjection.pushModules,
            InAppInjection.inAppModules,
            DeepLinkInjection.deepLinkModules,
            EmbeddedMessagingInjection.embeddedMessagingModules
        )
    }

    val koin = koinApp.koin

    fun init(): Koin {
        if (runningApp == null) {
            koinApp.koin.loadModules(loadPlatformModules())
            runningApp = startKoin(koinApp)
            val sdkDispatcher = koin.get<CoroutineDispatcher>(named(DispatcherTypes.Sdk))
            val sdkContext = koin.get<SdkContextApi>()
            val initOrganizer = koin.get<InitOrganizerApi>()
            val logger =
                koin.get<Logger> { parametersOf(SdkKoinIsolationContext::class.simpleName) }
            CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
                logger.debug("SDK DI initialized")
                if (sdkContext.currentSdkState.value == SdkState.UnInitialized) {
                    initOrganizer.init()
                    logger.debug("SDK initialized with InitOrganizer")
                }
            }
        }

        return koin
    }
}

expect fun SdkKoinIsolationContext.loadPlatformModules(): List<Module>