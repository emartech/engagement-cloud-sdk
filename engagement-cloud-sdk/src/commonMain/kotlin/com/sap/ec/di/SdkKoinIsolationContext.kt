package com.sap.ec.di

import com.sap.ec.InternalSdkApi
import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.init.InitOrganizerApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication

@InternalSdkApi
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

    fun init() {
        if (runningApp == null) {
            initKoin()
            val sdkDispatcher = koin.get<CoroutineDispatcher>(named(DispatcherTypes.Sdk))
            CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
                launchInitOrganizer()
            }
        }
    }

    suspend fun initLaunch() {
        if (runningApp == null) {
            initKoin()
            launchInitOrganizer()
        }
    }

    private suspend fun launchInitOrganizer() {
        val sdkContext = koin.get<SdkContextApi>()
        val initOrganizer = koin.get<InitOrganizerApi>()
        val logger =
            koin.get<Logger> { parametersOf(SdkKoinIsolationContext::class.simpleName) }
        if (sdkContext.currentSdkState.value == SdkState.UnInitialized) {
            initOrganizer.init()
            logger.debug("SDK initialized with InitOrganizer")
        }
    }

    private fun initKoin() {
        koinApp.koin.loadModules(loadPlatformModules())
        runningApp = startKoin(koinApp)

    }
}

internal expect fun SdkKoinIsolationContext.loadPlatformModules(): List<Module>