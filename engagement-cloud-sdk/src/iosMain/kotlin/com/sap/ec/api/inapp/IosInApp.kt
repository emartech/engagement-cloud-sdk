package com.sap.ec.api.inapp

import androidx.compose.ui.window.ComposeUIViewController
import com.sap.ec.di.SdkKoinIsolationContext.koin
import platform.UIKit.UIViewController
import com.sap.ec.mobileengage.inapp.view.InlineInAppView as ComposeInlineInAppView

class IosInApp: IosInAppApi {
    override val isPaused: Boolean
        get() {
            return koin.get<InAppApi>().isPaused
        }

    override suspend fun pause() {
        koin.get<InAppApi>().pause()
    }

    override suspend fun resume() {
        koin.get<InAppApi>().resume()
    }

    override fun InlineInAppViewController(
        viewId: String,
        onLoaded: (() -> Unit)?,
        onClose: (() -> Unit)?
    ): UIViewController {
        return ComposeUIViewController {
            ComposeInlineInAppView(viewId, onLoaded, onClose)
        }
    }
}