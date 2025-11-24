package com.emarsys.core.providers.pagelocation

internal expect class PageLocationProvider : PageLocationProviderApi {
    override fun provide(): String
}