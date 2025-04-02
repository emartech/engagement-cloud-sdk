package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.SuspendProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.UIKit.UIScene
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

internal class WindowProvider(
    private val sceneProvider: Provider<UIScene>,
    private val viewControllerProvider: Provider<UIViewController>,
    private val mainDispatcher: CoroutineDispatcher
) : SuspendProvider<UIWindow> {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun provide(): UIWindow {
        val window = withContext(mainDispatcher) {
            val window = UIWindow(sceneProvider.provide() as UIWindowScene)
            val width: Int = window.frame.size
            val height: Int = window.frame.size
            if (width == 0 && height == 0) {
                val screenBounds = UIScreen.mainScreen.bounds
                window.setFrame(screenBounds)
            }
            window.setRootViewController(viewControllerProvider.provide())
            window
        }
        return window
    }

}