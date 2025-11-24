package com.emarsys.core.providers

import kotlinx.browser.window

internal class WebLanguageProvider: LanguageProviderApi {
    override fun provide(): String {
        return window.navigator.language
    }
}