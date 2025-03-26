package com.emarsys.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

internal interface SdkComponent: KoinComponent {
    override fun getKoin(): Koin = SdkKoinIsolationContext.koin
}
