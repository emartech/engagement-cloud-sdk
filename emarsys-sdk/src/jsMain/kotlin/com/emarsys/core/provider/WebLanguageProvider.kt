package com.emarsys.core.provider

import com.emarsys.core.providers.LanguageProviderApi
import kotlinx.browser.window

internal class WebLanguageProvider: LanguageProviderApi {
    override fun provide(): String {
        return window.navigator.language
    }
}