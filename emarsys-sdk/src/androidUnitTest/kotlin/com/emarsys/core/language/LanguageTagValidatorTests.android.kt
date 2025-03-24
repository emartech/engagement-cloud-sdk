package com.emarsys.core.language

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LanguageTagValidatorTests {

    private lateinit var languageTagValidator: LanguageTagValidator

    @BeforeTest
    fun setup() = runTest {
        languageTagValidator = LanguageTagValidator()
    }

    @Test
    fun testIsValid_shouldReturnTrue() = runTest {
        languageTagValidator.isValid("zh-Hans-CN") shouldBe true
    }

    @Test
    fun testIsValid_shouldReturnFalse() = runTest {
        languageTagValidator.isValid("invalid") shouldBe false
    }

}