package com.emarsys.sample

import EmarsysJs
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val emarsys = EmarsysJs
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}