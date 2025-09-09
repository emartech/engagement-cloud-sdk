package com.emarsys.di

import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import org.koin.dsl.module

object EmbeddedMessagingInjection {
    val embeddedMessagingModules = module {
        single<EmbeddedMessagingRequestFactoryApi> {
            EmbeddedMessagesRequestFactory(
                get(),
                get()
            )
        }
    }
}