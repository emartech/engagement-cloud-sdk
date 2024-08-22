package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.providers.Provider
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

class ViewControllerProvider : Provider<UIViewController> {
    override fun provide(): UIViewController {
        val viewController = UIViewController()
        viewController.view.setBackgroundColor(UIColor.clearColor)
        return viewController
    }
}