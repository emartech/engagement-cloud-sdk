package com.sap.ec.core.providers.pagelocation

internal expect class PageLocationProvider : PageLocationProviderApi {
    override fun provide(): String
}