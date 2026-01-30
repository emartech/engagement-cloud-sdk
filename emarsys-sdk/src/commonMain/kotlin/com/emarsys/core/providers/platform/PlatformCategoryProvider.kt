package com.emarsys.core.providers.platform

internal expect class PlatformCategoryProvider: PlatformCategoryProviderApi {
    override fun provide(): String
}