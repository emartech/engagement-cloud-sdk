package com.emarsys.di

import com.emarsys.JsEmarsysConfig
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
import com.emarsys.core.badge.WebBadgeCountHandler
import com.emarsys.core.badge.WebBadgeCountHandlerApi
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.mobileengage.push.PushNotificationClickHandler
import com.emarsys.mobileengage.push.PushNotificationClickHandlerApi
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.JsEmarsysConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import web.broadcast.BroadcastChannel

object WebInjection {
    val webModules = module {
        single<StringStorageApi> { StringStorage(window.localStorage) }
        single<SdkConfigStoreApi<JsEmarsysConfig>> {
            JsEmarsysConfigStore(
                typedStorage = get()
            )
        }
        single<PushNotificationClickHandlerApi> {
            PushNotificationClickHandler(
                actionFactory = get(),
                actionHandler = get(),
                onNotificationClickedBroadcastChannel = BroadcastChannel(
                    WEB_PUSH_ON_NOTIFICATION_CLICKED_CHANNEL_NAME
                ),
                coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                sdkLogger = get { parametersOf(PushNotificationClickHandler::class.simpleName) }
            )
        }
        single<WebBadgeCountHandlerApi> {
            WebBadgeCountHandler(
                onBadgeCountUpdateReceivedBroadcastChannel = BroadcastChannel(
                    WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
                ),
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
                coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                sdkLogger = get { parametersOf(WebBadgeCountHandler::class.simpleName) }

            )
        }
        single<PlatformInitializerApi> {
            PlatformInitializer(
                pushNotificationClickHandler = get(),
                webBadgeCountHandler = get()
            )
        }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(WebInjection.webModules)
}