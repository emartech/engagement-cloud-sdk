package com.sap.ec.mobileengage.inapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sap.ec.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest

class InAppDialogTests {

    private lateinit var dialog: InAppDialog
    private lateinit var mockInflater: LayoutInflater
    private lateinit var mockInAppContainer: FrameLayout

    @Before
    fun setup() {
        mockInAppContainer = mockk(relaxed = true)
        val mockRootView = mockk<View> {
            every { findViewById<FrameLayout>(R.id.mobileEngageInAppMessageContainer) } returns mockInAppContainer
        }
        mockInflater = mockk {
            every { inflate(R.layout.mobile_engage_in_app_message, any(), false) } returns mockRootView
        }
        dialog = InAppDialog()
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onCreateView_shouldAddInAppView_toContainer_whenInAppViewHasNoParent() {
        val mockInAppView = mockk<View>(relaxed = true) {
            every { parent } returns null
        }
        dialog.setInAppView(mockInAppView)

        dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

        verify(exactly = 0) { mockInAppContainer.removeView(mockInAppView) }
        verify { mockInAppContainer.addView(mockInAppView) }
    }

    @Test
    fun onCreateView_shouldRemoveInAppView_fromOldParent_beforeAddingToContainer() {
        val mockOldParent = mockk<ViewGroup>(relaxed = true)
        val mockInAppView = mockk<View>(relaxed = true) {
            every { parent } returns mockOldParent
        }
        dialog.setInAppView(mockInAppView)

        dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

        verify { mockOldParent.removeView(mockInAppView) }
        verify { mockInAppContainer.addView(mockInAppView) }
    }

    @Test
    fun onCreateView_shouldNotAddAnyView_whenInAppViewIsNotSet() {
        dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

        verify(exactly = 0) { mockInAppContainer.addView(any()) }
    }
}
