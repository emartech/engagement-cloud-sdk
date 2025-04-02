package com.emarsys.core.language

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebLanguageTagValidatorTests {

    private lateinit var webLanguageTagValidator: WebLanguageTagValidator

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        webLanguageTagValidator = WebLanguageTagValidator()
    }

    @Test
    fun testIsValid_shouldReturnTrue() = runTest {
        webLanguageTagValidator.isValid("zh-Hans-CN") shouldBe true
    }

    @Test
    fun testIsValid_shouldReturnFalse() = runTest {
        webLanguageTagValidator.isValid("invalid") shouldBe false
    }

}