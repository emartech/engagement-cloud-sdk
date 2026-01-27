package com.emarsys.di

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationState
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.PagerFactory
import com.emarsys.mobileengage.embeddedmessaging.ui.list.PagerFactoryApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object EmbeddedMessagingInjection {
    val embeddedMessagingModules = module {
        single<EmbeddedMessagingRequestFactoryApi> {
            EmbeddedMessagesRequestFactory(
                urlFactory = get(),
                json = get()
            )
        }
        single<EmbeddedMessagingContextApi> {
            EmbeddedMessagingContext()
        }
        single<ListPageModelApi> {
            ListPageModel(
                sdkEventDistributor = get(),
                sdkLogger = get { parametersOf(ListPageModel::class.simpleName) },
                paginationState = EmbeddedMessagingPaginationState()
            )
        }
        single<PagerFactoryApi> {
            PagerFactory(
                model = get<ListPageModelApi>(),
                downloaderApi = get<DownloaderApi>(),
                sdkEventDistributor = get<SdkEventDistributorApi>(),
                actionFactory = get<EventActionFactoryApi>(),
                logger = get { parametersOf(PagerFactory::class.simpleName) }
            )
        }

        single<ListPageViewModelApi> {
            ListPageViewModel(
                embeddedMessagingContext = get(),
                timestampProvider = get(),
                coroutineScope = get<CoroutineScope>(named(CoroutineScopeTypes.Application)),
                pagerFactory = get(),
                connectionWatchDog = get<ConnectionWatchDog>(),
                locallyDeletedMessageIds = MutableStateFlow(emptySet()),
                locallyOpenedMessageIds = MutableStateFlow(emptySet())
            )
        }
    }
}