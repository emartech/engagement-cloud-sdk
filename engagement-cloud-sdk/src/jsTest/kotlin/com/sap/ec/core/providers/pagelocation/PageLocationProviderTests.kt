package com.sap.ec.core.providers.pagelocation

import io.kotest.matchers.shouldNotBe
import io.ktor.http.Url
import kotlin.test.Test

class PageLocationProviderTests {

    @Test
    fun provide_shouldReturn_aValidHrefUrl() {
        val location = PageLocationProvider().provide()

        Url(location) shouldNotBe null
    }
}