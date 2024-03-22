package com.emarsys.core.device

import io.kotest.matchers.shouldBe
import java.util.Locale
import kotlin.test.Test


class AndroidLanguageProviderTests {
    private lateinit var languageProvider: LanguageProvider

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenOnlyLanguageUsed() {
        languageProvider = AndroidLanguageProvider(Locale("en"))
        val result = languageProvider.provideLanguage()

        result shouldBe "en"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenLanguageAndCountryUsed() {
        languageProvider = AndroidLanguageProvider(Locale("en", "US"))
        val result = languageProvider.provideLanguage()

        result shouldBe "en-US"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenLocaleBuilderUsed() {
        languageProvider =
            AndroidLanguageProvider(Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build())
        val result = languageProvider.provideLanguage()

        result shouldBe "zh-Hans-CN"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode() {
        languageProvider = AndroidLanguageProvider(Locale.SIMPLIFIED_CHINESE)
        val result = languageProvider.provideLanguage()

        result shouldBe "zh-CN"
    }
}