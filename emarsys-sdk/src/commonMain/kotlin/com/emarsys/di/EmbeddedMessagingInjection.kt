package com.emarsys.di

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import kotlinx.coroutines.CoroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object EmbeddedMessagingInjection {
    val embeddedMessagingModules = module {
        single<EmbeddedMessagingRequestFactoryApi> {
            EmbeddedMessagesRequestFactory(
                get(),
                get()
            )
        }
        single<EmbeddedMessagingContextApi>{
            EmbeddedMessagingContext()
        }
        single<ListPageModelApi> {
            ListPageModel(
                sdkEventDistributor = get(),
                sdkLogger = get{ parametersOf(ListPageModel::class.simpleName) }
            )
        }
        factory<ListPageViewModelApi> {
            ListPageViewModel(
                model = get<ListPageModelApi>(),
                downloaderApi = get<DownloaderApi>(),
                sdkEventDistributor = get<SdkEventDistributorApi>(),
                coroutineScope = get<CoroutineScope>(named(CoroutineScopeTypes.Application))
            )
        }
    }
}