package com.sap.ec.core.language

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AndroidLanguageTagValidatorTests {

    private lateinit var androidLanguageTagValidator: AndroidLanguageTagValidator

    @BeforeTest
    fun setup() = runTest {
        androidLanguageTagValidator = AndroidLanguageTagValidator()
    }

    @Test
    fun testIsValid_shouldReturnTrue() = runTest {
        androidLanguageTagValidator.isValid("zh-Hans-CN") shouldBe true
    }

    @Test
    fun testIsValid_shouldReturnFalse() = runTest {
        androidLanguageTagValidator.isValid("invalid") shouldBe false
    }

}