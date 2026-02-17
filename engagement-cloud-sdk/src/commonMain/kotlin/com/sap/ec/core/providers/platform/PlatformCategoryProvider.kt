package com.sap.ec.core.providers.platform

internal expect class PlatformCategoryProvider: PlatformCategoryProviderApi {
    override fun provide(): String
}