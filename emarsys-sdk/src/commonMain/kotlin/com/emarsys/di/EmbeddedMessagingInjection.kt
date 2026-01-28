package com.emarsys.di

import com.emarsys.api.embeddedmessaging.EmbeddedMessaging
import com.emarsys.api.embeddedmessaging.EmbeddedMessagingApi
import com.emarsys.api.embeddedmessaging.EmbeddedMessagingInstance
import com.emarsys.api.embeddedmessaging.EmbeddedMessagingInternal
import com.emarsys.api.embeddedmessaging.LoggingEmbeddedMessaging
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
        single<EmbeddedMessagingInstance>(named(InstanceType.Logging)) {
            LoggingEmbeddedMessaging(
                logger = get { parametersOf(LoggingEmbeddedMessaging::class.simpleName) },
                sdkContext = get()
            )
        }
        single<EmbeddedMessagingInstance>(named(InstanceType.Internal)) {
            EmbeddedMessagingInternal(
                listPageViewModel = get(),
                sdkLogger = get { parametersOf(EmbeddedMessagingInternal::class.simpleName) },
            )
        }
        single<EmbeddedMessagingApi> {
            EmbeddedMessaging(
                logging = get(named(InstanceType.Logging)),
                gatherer = get(named(InstanceType.Logging)),
                internal = get(named(InstanceType.Internal)),
                sdkContext = get()
            )
        }
    }
}