package com.emarsys.di

import com.emarsys.api.push.PushApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.language.LanguageTagValidatorApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

object PlatformInjection {
    val platformModules = module {
        single<ClipboardHandlerApi> { get<DependencyCreator>().createClipboardHandler() }
        single<LaunchApplicationHandlerApi> { get<DependencyCreator>().createLaunchApplicationHandler() }
        single<LanguageTagValidatorApi> { get<DependencyCreator>().createLanguageTagValidator() }
        single<PushApi> {
            get<DependencyCreator>().createPushApi(
                pushInternal = get(named(InstanceType.Internal)),
                get(),
                get()
            )
        }
    }
}
