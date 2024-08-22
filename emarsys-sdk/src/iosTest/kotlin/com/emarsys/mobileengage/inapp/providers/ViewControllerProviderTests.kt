package com.emarsys.mobileengage.inapp.providers

import io.kotest.matchers.shouldBe
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import kotlin.test.Test

class ViewControllerProviderTests {

    @Test
    fun testProvideReturnsUIViewController() {

        val provider = ViewControllerProvider()

        val viewController = provider.provide()

        (viewController is UIViewController) shouldBe true
        viewController.view.backgroundColor shouldBe UIColor.clearColor
    }
}
