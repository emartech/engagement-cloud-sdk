package com.sap.ec.mobileengage.inapp.providers

import io.kotest.matchers.shouldBe
import platform.UIKit.UIColor
import kotlin.test.Test

class ViewControllerProviderTests {

    @Test
    fun testProvideReturnsUIViewController() {

        val provider = ViewControllerProvider()

        val viewController = provider.provide()

        viewController.view.backgroundColor shouldBe UIColor.clearColor
    }
}
