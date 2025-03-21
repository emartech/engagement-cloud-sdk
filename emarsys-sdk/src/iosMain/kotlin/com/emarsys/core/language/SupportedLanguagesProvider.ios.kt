package com.emarsys.core.language

import com.emarsys.core.providers.Provider
import platform.Foundation.NSBundle

class SupportedLanguagesProvider: Provider<List<String>> {

    override fun provide(): List<String> {
        return NSBundle.mainBundle.localizations as List<String>
    }
}