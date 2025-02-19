package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.UIKit.NSLayoutAttributeHeight
import platform.UIKit.NSLayoutAttributeLeft
import platform.UIKit.NSLayoutAttributeTop
import platform.UIKit.NSLayoutAttributeWidth
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutRelationEqual
import platform.UIKit.UIApplication
import platform.UIKit.UIView
import platform.UIKit.UIWindow

class InAppPresenter(
    private val windowProvider: WindowProvider,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkEventFlow: SharedFlow<SdkEvent>
) : InAppPresenterApi {
    override suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val window = windowProvider.provide()
        val webView = (webViewHolder as IosWebViewHolder).webView
        window.addView(webView)

        val originalWindow = withContext(CoroutineScope(mainDispatcher).coroutineContext) {
            val originalWindow =
                UIApplication.sharedApplication.windows.filter { (it as UIWindow).isKeyWindow() }
                    .map { it as UIWindow }.firstOrNull()
            window.makeKeyAndVisible()
            originalWindow
        }
        CoroutineScope(sdkDispatcher).launch {
            sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Dismiss && it.campaignId == inAppView.inAppMessage.campaignId }

            withContext(mainDispatcher) {
                window.removeFromSuperview()
                originalWindow?.makeKeyAndVisible()
            }

        }
    }

    fun UIWindow.addView(view: UIView) {
        CoroutineScope(mainDispatcher).launch {

            rootViewController?.view?.addSubview(view)
            view.translatesAutoresizingMaskIntoConstraints = false

            val topConstraint = NSLayoutConstraint.constraintWithItem(
                view,
                NSLayoutAttributeTop,
                NSLayoutRelationEqual,
                rootViewController?.view,
                NSLayoutAttributeTop,
                1.0,
                0.0
            )

            val leftConstraint = NSLayoutConstraint.constraintWithItem(
                view,
                NSLayoutAttributeLeft,
                NSLayoutRelationEqual,
                rootViewController?.view,
                NSLayoutAttributeLeft,
                1.0,
                0.0
            )

            val widthConstraint = NSLayoutConstraint.constraintWithItem(
                view,
                NSLayoutAttributeWidth,
                NSLayoutRelationEqual,
                rootViewController?.view,
                NSLayoutAttributeWidth,
                1.0,
                0.0
            )

            val heightConstraint = NSLayoutConstraint.constraintWithItem(
                view,
                NSLayoutAttributeHeight,
                NSLayoutRelationEqual,
                rootViewController?.view,
                NSLayoutAttributeHeight,
                1.0,
                0.0
            )

            rootViewController?.view?.addConstraints(
                listOf(
                    topConstraint,
                    leftConstraint,
                    widthConstraint,
                    heightConstraint
                )
            )
            rootViewController?.view?.layoutIfNeeded()
        }
    }
}