package com.sap.ec.mobileengage.inapp.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.sap.ec.R

internal class InAppDialog : DialogFragment() {

	companion object {
		const val TAG = "MOBILE_ENGAGE_IN_APP_DIALOG_TAG"

		@Volatile
		internal var retainedView: View? = null
	}

	fun setInAppView(inAppView: InAppViewApi) {
		retainedView = inAppView as View
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view =
			inflater.inflate(R.layout.mobile_engage_in_app_message, container, false)
		val inAppContainer: FrameLayout = view.findViewById(R.id.mobileEngageInAppMessageContainer)
		retainedView?.let {
			(it.parent as? ViewGroup)?.removeView(it)
			inAppContainer.addView(it)
		}
		return inAppContainer
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = super.onCreateDialog(savedInstanceState)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		return dialog
	}

	override fun onDestroy() {
		super.onDestroy()
		if (activity?.isChangingConfigurations != true) {
			retainedView = null
		}
	}
}
