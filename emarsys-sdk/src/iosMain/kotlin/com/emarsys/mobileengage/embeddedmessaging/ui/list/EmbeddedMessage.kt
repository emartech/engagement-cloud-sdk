package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun embeddedMessage(): UIViewController {
    return ComposeUIViewController {
        ListPageView()
    }
}