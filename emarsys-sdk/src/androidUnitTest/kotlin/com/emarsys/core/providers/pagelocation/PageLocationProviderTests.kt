package com.emarsys.core.providers.pagelocation

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PageLocationProviderTests {

    @Test
    fun provide_shouldReturnEmptyString() {
        PageLocationProvider().provide() shouldBe ""
    }
}