package com.emarsys.core.provider

import com.emarsys.core.providers.Provider
import kotlinx.browser.window

class WebLanguageProvider: Provider<String> {
    override fun provide(): String {
        return window.navigator.language
    }
}