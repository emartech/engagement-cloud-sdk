package com.sap.ec.core.providers.pagelocation

import kotlinx.browser.window

internal actual class PageLocationProvider : PageLocationProviderApi {
    actual override fun provide(): String {
        return window.location.href
    }
}