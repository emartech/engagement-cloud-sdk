package com.emarsys.core.device

import com.emarsys.core.providers.Provider
import java.util.Locale

class AndroidLanguageProvider(private val locale: Locale) : Provider<String> {
    override fun provide(): String {
        return locale.toLanguageTag()
    }
}
