package com.sap.ec.sample

import JSEngagementCloud
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val engagementCloud = JSEngagementCloud
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}