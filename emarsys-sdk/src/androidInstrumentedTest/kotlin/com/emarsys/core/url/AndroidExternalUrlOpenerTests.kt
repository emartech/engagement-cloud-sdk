package com.emarsys.core.url

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.url.AndroidExternalUrlOpener
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class AndroidExternalUrlOpenerTests {

    private lateinit var mockContext: Context
    private lateinit var mockLogger: Logger
    private lateinit var androidExternalUrlOpener: AndroidExternalUrlOpener

    @Before
    fun setup() {
        mockContext = mockk<Context>(relaxed = true)
        mockLogger = mockk(relaxed = true)
        androidExternalUrlOpener = AndroidExternalUrlOpener(mockContext, mockLogger)
    }

    @Test
    fun testOpen_shouldOpenValidUrl() = runTest {
        val testActionModel = BasicOpenExternalUrlActionModel(
            url = "https://www.sap.com"
        )
        val intentCaptor = slot<Intent>()
        every { mockContext.startActivity(capture(intentCaptor)) } returns Unit

        androidExternalUrlOpener.open(testActionModel)

        verify { mockContext.startActivity(any<Intent>()) }
        val capturedIntent = intentCaptor.captured
        capturedIntent.action shouldBe Intent.ACTION_VIEW
        capturedIntent.flags shouldBe FLAG_ACTIVITY_NEW_TASK
    }

    @Test
    fun testOpen_shouldNotOpenInvalidUrl() = runTest {
        val testActionModel = BasicOpenExternalUrlActionModel(
            url = "invalid"
        )

        androidExternalUrlOpener.open(testActionModel)

        verify(exactly = 0) { mockContext.startActivity(any<Intent>()) }
    }


    @Test
    fun testOpen_shouldNotOpenValidUrlWhenActivityIsNotFound() = runTest {
        val url = "https://www.sap.com"
        val testActionModel = BasicOpenExternalUrlActionModel(
            url = url
        )
        val exception = ActivityNotFoundException()
        every { mockContext.startActivity(any()) } throws exception

        androidExternalUrlOpener.open(testActionModel)

        verifySuspend {
            mockLogger.error(
                "AndroidExternalUrlOpener",
                exception,
                buildJsonObject { put("url", JsonPrimitive(url)) }
            )
        }
    }

}