package com.emarsys.core.language

import com.emarsys.core.providers.Provider
import kotlinx.browser.window

class SupportedLanguagesProvider: Provider<List<String>> {

    override fun provide(): List<String> {
        return window.navigator.languages.toList()
    }
}
