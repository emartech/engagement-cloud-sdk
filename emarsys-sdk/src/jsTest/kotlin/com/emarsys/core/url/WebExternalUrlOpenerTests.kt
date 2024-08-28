package com.emarsys.core.url

import com.emarsys.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class WebExternalUrlOpenerTests {

    private lateinit var webExternalUrlOpener: WebExternalUrlOpener

    @BeforeTest
    fun setup() {
        val mockLogger = mock<Logger> {
            everySuspend {
                error(
                    any<String>(),
                    any<Throwable>(),
                    any<Map<String, Any>>()
                )
            } returns Unit
        }
        webExternalUrlOpener = WebExternalUrlOpener(window, mockLogger)
    }


    @Ignore
    @Test
    fun open_shouldReturnTrue_whenCalledWithValidUrl() = runTest {
        val url = "https://www.google.com"

        val result = webExternalUrlOpener.open(url)

        result shouldBe true
    }

    @Test
    fun open_shouldReturnFalse_whenCalledWithInvalidUrl() = runTest {
        val url = "invalidUrl"

        val result = webExternalUrlOpener.open(url)

        result shouldBe false
    }
}