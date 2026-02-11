package com.emarsys.mobileengage.inapp.iframe

import io.kotest.matchers.shouldBe
import web.dom.ElementId
import web.dom.document
import kotlin.test.Test

class IframeContainerResizerTests {

    @Test
    fun resize_shouldResizeTheComponent_withTheProvidedId() {
        val iframeContainerResizer = IframeContainerResizer()
        val testId = "testId"
        val doNotResizeId = "doNotResize"
        val initialHeight = "123px"
        val testHeight = 321

        val testDiv = document.createElement("div").apply {
            id = ElementId(testId)
            style.height = initialHeight
        }

        val doNotResizeDiv = document.createElement("div").apply {
            id = ElementId(doNotResizeId)
            style.height = initialHeight
        }

        document.body.appendChild(testDiv)
        document.body.appendChild(doNotResizeDiv)

        iframeContainerResizer.resize(testId, testHeight)

        document.getElementById(ElementId(testId))?.style?.height shouldBe "${testHeight}px"
        document.getElementById(ElementId(doNotResizeId))?.style?.height shouldBe initialHeight
    }
}