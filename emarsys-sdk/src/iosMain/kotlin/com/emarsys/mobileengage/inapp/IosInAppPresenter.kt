package com.emarsys.mobileengage.inapp

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.inapp.providers.WindowProvider
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import platform.UIKit.NSLayoutAttributeHeight
import platform.UIKit.NSLayoutAttributeLeft
import platform.UIKit.NSLayoutAttributeTop
import platform.UIKit.NSLayoutAttributeWidth
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutRelationEqual
import platform.UIKit.UIApplication
import platform.UIKit.UIView
import platform.UIKit.UIWindow

internal class IosInAppPresenter(
    private val windowProvider: WindowProvider,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val logger: Logger
) : InAppPresenterApi {
    override suspend fun trackMetric(
        campaignId: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    ) {
        logger.metric(
            message = "InAppMetric",
            data = buildJsonObject {
                put("campaignId", JsonPrimitive(campaignId))
                put(
                    "loadingTimeStart",
                    JsonPrimitive(loadingMetric.loadingStarted)
                )
                put(
                    "loadingTimeEnd",
                    JsonPrimitive(loadingMetric.loadingEnded)
                )
                put(
                    "loadingTimeDuration",
                    JsonPrimitive(loadingMetric.loadingEnded - loadingMetric.loadingStarted)
                )
                put("onScreenTimeStart", JsonPrimitive(onScreenTimeStart))
                put("onScreenTimeEnd", JsonPrimitive(onScreenTimeEnd))
                put(
                    "onScreenTimeDuration",
                    JsonPrimitive(onScreenTimeEnd - onScreenTimeStart)
                )
            }
        )
    }

    override suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val window = windowProvider.provide()
        val webView = (webViewHolder as IosWebViewHolder).webView
        window.addView(webView)

        val originalWindow = withContext(mainDispatcher) {
            val originalWindow =
                UIApplication.sharedApplication.windows.filter { (it as UIWindow).isKeyWindow() }
                    .map { it as UIWindow }.firstOrNull()
            window.makeKeyAndVisible()
            originalWindow
        }
        CoroutineScope(sdkDispatcher).launch {
            sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Dismiss && it.id == inAppView.inAppMessage.campaignId }

            withContext(mainDispatcher) {
                window.removeFromSuperview()
                originalWindow?.makeKeyAndVisible()
            }

        }
    }

    private suspend fun UIWindow.addView(view: UIView) {
        withContext(mainDispatcher) {

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