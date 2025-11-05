package com.emarsys.di

import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
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
    }
}