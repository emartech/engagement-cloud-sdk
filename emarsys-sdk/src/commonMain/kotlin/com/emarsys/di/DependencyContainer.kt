package com.emarsys.di

import PlatformContext
import com.emarsys.core.storage.Storage

class DependencyContainer : DependencyContainerApi {

    val platformContext: PlatformContext = CommonPlatformContext()

    val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    val storage: Storage by lazy { dependencyCreator.createStringStorage() }
}