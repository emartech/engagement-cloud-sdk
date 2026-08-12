package com.sap.ec.mobileengage.inapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.sap.ec.R
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertNotNull

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
		InAppDialog.retainedView = null
	}

	@AfterTest
	fun tearDown() {
		InAppDialog.retainedView = null
		unmockkAll()
	}

	@Test
	fun onCreateView_shouldReturnInAppContainer() {
		val result = dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

		assertNotNull(result)
		verify { mockInflater.inflate(R.layout.mobile_engage_in_app_message, any(), false) }
	}

	@Test
	fun onCreateView_shouldAddInAppView_toContainer_whenInAppViewHasNoParent() {
		val mockView = mockk<View>(relaxed = true) {
			every { parent } returns null
		}
		InAppDialog.retainedView = mockView

		dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

		verify { mockInAppContainer.addView(mockView) }
	}

	@Test
	fun onCreateView_shouldRemoveInAppView_fromOldParent_beforeAddingToContainer() {
		val mockOldParent = mockk<ViewGroup>(relaxed = true)
		val mockView = mockk<View>(relaxed = true) {
			every { parent } returns mockOldParent
		}
		InAppDialog.retainedView = mockView

		dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

		verify { mockOldParent.removeView(mockView) }
		verify { mockInAppContainer.addView(mockView) }
	}

	@Test
	fun onCreateView_shouldNotAddAnyView_whenInAppViewIsNotSet() {
		dialog.onCreateView(mockInflater, mockk(relaxed = true), Bundle())

		verify(exactly = 0) { mockInAppContainer.addView(any()) }
	}

	@Test
	fun onDestroy_shouldClearRetainedView_whenNotConfigurationChange() {
		val mockView = mockk<View>(relaxed = true)
		InAppDialog.retainedView = mockView

		val mockActivity = mockk<FragmentActivity> {
			every { isChangingConfigurations } returns false
		}
		val dialogSpy = spyk(dialog)
		every { dialogSpy.activity } returns mockActivity

		dialogSpy.onDestroy()

		InAppDialog.retainedView.shouldBeNull()
	}

	@Test
	fun onDestroy_shouldRetainView_whenIsConfigurationChange() {
		val mockView = mockk<View>(relaxed = true)
		InAppDialog.retainedView = mockView

		val mockActivity = mockk<FragmentActivity> {
			every { isChangingConfigurations } returns true
		}
		val dialogSpy = spyk(dialog)
		every { dialogSpy.activity } returns mockActivity

		dialogSpy.onDestroy()

		InAppDialog.retainedView.shouldNotBeNull()
	}

	@Test
	fun onDestroy_shouldClearRetainedView_whenActivityIsNull() {
		val mockView = mockk<View>(relaxed = true)
		InAppDialog.retainedView = mockView

		val dialogSpy = spyk(dialog)
		every { dialogSpy.activity } returns null

		dialogSpy.onDestroy()

		InAppDialog.retainedView.shouldBeNull()
	}
}
