package com.sap.ec.core.providers

import kotlinx.browser.window

internal class WebLanguageProvider: LanguageProviderApi {
    override fun provide(): String {
        return window.navigator.language
    }
}