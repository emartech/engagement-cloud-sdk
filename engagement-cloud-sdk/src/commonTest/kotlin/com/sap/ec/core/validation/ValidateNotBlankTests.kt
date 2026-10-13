package com.sap.ec.core.validation

import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.Test

class ValidateNotBlankTests {

    @Test
    fun validateNotBlank_shouldNotThrow_ifStringIsNotBlank() {
        "testString".validateNotBlank()
    }

    @Test
    fun validateNotBlank_shouldThrow_IllegalArgumentException_ifStringIsBlank() {
        shouldThrow<IllegalArgumentException> { "".validateNotBlank() }
    }

    @Test
    fun validateNotBlank_shouldThrow_IllegalArgumentException_ifStringContainsOnlyWhiteSpaces() {
        shouldThrow<IllegalArgumentException> { "      ".validateNotBlank() }
    }
}