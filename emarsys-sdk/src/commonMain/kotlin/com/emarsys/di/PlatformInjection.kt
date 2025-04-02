package com.emarsys.di

import com.emarsys.api.push.PushApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

object PlatformInjection {
    val platformModules = module {
        single<PushApi> {
            get<DependencyCreator>().createPushApi(
                pushInternal = get(named(InstanceType.Internal)),
                get(),
                get()
            )
        }
    }
}
