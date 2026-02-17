package com.sap.ec.iosNotificationService.provider

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SessionProviderTests {

    private lateinit var sessionProvider: SessionProvider

    @BeforeTest
    fun setup() {
        sessionProvider = SessionProvider()
    }

    @Test
    fun provide_shouldReturn_UrlSession() {
        val result = sessionProvider.provide()

        result shouldNotBe null
    }

    @Test
    fun provide_should_returnSameResults() {
        val result1 = sessionProvider.provide()
        val result2 = sessionProvider.provide()

        result1 shouldBe result2
    }

}
