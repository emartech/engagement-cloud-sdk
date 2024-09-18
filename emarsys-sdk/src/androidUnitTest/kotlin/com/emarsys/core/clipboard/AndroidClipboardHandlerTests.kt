package com.emarsys.core.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import com.emarsys.mobileengage.clipboard.AndroidClipboardHandler
import com.emarsys.mobileengage.clipboard.AndroidClipboardHandler.Companion.TEXT_LABEL
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AndroidClipboardHandlerTests {

    @Test
    fun copyToClipboard_shouldSetClipboardContent() = runTest {
        val testText = "test"
        mockkStatic("android.content.ClipData")
        val mockClipData = mockk<ClipData>()
        every { ClipData.newPlainText(TEXT_LABEL, testText) } returns mockClipData
        val mockClipboardManager = mockk<ClipboardManager> {
            every { setPrimaryClip(any<ClipData>()) } returns Unit
        }

        AndroidClipboardHandler(mockClipboardManager).copyToClipboard(testText)

        verify {
            mockClipboardManager.setPrimaryClip(mockClipData)
        }
    }

}