package com.emarsys.core.url

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AndroidExternalUrlOpenerTests {

    private lateinit var mockContext: Context

    private lateinit var androidExternalUrlOpener: AndroidExternalUrlOpener

    @Before
    fun setup() {
        mockContext = mockk<Context>(relaxed = true)
        androidExternalUrlOpener = AndroidExternalUrlOpener(mockContext)
    }

    @Test
    fun testOpen_shouldOpenValidUrl() = runTest {
        val intentCaptor = slot<Intent>()
        every { mockContext.startActivity(capture(intentCaptor)) } returns Unit

        val result = androidExternalUrlOpener.open("https://www.emarsys.com")

        result shouldBe true
        verify { mockContext.startActivity(any<Intent>()) }
        val capturedIntent = intentCaptor.captured
        capturedIntent.action shouldBe Intent.ACTION_VIEW
        capturedIntent.flags shouldBe FLAG_ACTIVITY_NEW_TASK
    }

    @Test
    fun testOpen_shouldNotOpenInvalidUrl() = runTest {
        val result = androidExternalUrlOpener.open("invalid")

        result shouldBe false
        verify(exactly = 0) { mockContext.startActivity(any<Intent>()) }
    }


    @Test
    fun testOpen_shouldNotOpenValidUrlWhenActivityIsNotFound() = runTest {
        every { mockContext.startActivity(any()) } throws ActivityNotFoundException()

        val result = androidExternalUrlOpener.open("https://www.emarsys.com")

        result shouldBe false
    }

}