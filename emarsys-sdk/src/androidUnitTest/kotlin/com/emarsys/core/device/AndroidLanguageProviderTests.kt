package com.emarsys.core.device

import com.emarsys.core.providers.Provider
import io.kotest.matchers.shouldBe
import java.util.Locale
import kotlin.test.Test


class AndroidLanguageProviderTests {
    private lateinit var languageProvider: Provider<String>

    @Test
    fun testProvide_shouldReturnCorrectLanguageCode_whenOnlyLanguageUsed() {
        languageProvider = AndroidLanguageProvider(Locale("en"))
        val result = languageProvider.provide()

        result shouldBe "en"
    }

    @Test
    fun testProvide_shouldReturnCorrectLanguageCode_whenLanguageAndCountryUsed() {
        languageProvider = AndroidLanguageProvider(Locale("en", "US"))
        val result = languageProvider.provide()

        result shouldBe "en-US"
    }

    @Test
    fun testProvide_shouldReturnCorrectLanguageCode_whenLocaleBuilderUsed() {
        languageProvider =
            AndroidLanguageProvider(Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build())
        val result = languageProvider.provide()

        result shouldBe "zh-Hans-CN"
    }

    @Test
    fun testProvide_shouldReturnCorrectLanguageCode() {
        languageProvider = AndroidLanguageProvider(Locale.SIMPLIFIED_CHINESE)
        val result = languageProvider.provide()

        result shouldBe "zh-CN"
    }
}