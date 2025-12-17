package com.emarsys.di

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationHandler
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationHandlerApi
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationState
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.PagerFactory
import com.emarsys.mobileengage.embeddedmessaging.ui.list.PagerFactoryApi
import kotlinx.coroutines.CoroutineScope
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
        single<EmbeddedMessagingPaginationHandlerApi> {
            EmbeddedMessagingPaginationHandler(
                sdkEventManager = get(),
                applicationScope = get<CoroutineScope>(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(EmbeddedMessagingPaginationHandler::class.simpleName) },
                paginationState = EmbeddedMessagingPaginationState()
            )
        }
        single<ListPageModelApi> {
            ListPageModel(
                sdkEventDistributor = get(),
                embeddedMessagingPaginationHandler = get(),
                sdkLogger = get { parametersOf(ListPageModel::class.simpleName) }
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
                pagerFactory = get()
            )
        }
    }
}