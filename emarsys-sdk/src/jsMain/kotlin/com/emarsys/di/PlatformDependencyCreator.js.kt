package com.emarsys.di

import PlatformContext
import com.emarsys.core.storage.Storage

actual class PlatformDependencyCreator actual constructor(platformContext: PlatformContext) : DependencyCreator {
    override fun createStringStorage(): Storage {
        TODO("Not yet implemented")
    }
}