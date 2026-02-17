package com.sap.ec.core.language

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosLanguageTagValidatorTests {

    private lateinit var iosLanguageTagValidator: IosLanguageTagValidator

    @BeforeTest
    fun setup() = runTest {
        iosLanguageTagValidator = IosLanguageTagValidator()
    }

    @Test
    fun testIsValid_shouldReturnTrue() = runTest {
        iosLanguageTagValidator.isValid("en-US") shouldBe true
    }

    @Test
    fun testIsValid_shouldReturnFalse() = runTest {
        iosLanguageTagValidator.isValid("invalid") shouldBe false
    }

}