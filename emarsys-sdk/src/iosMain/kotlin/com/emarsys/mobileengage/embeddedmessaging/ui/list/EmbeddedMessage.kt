package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.ui.window.ComposeUIViewController
import com.emarsys.di.SdkKoinIsolationContext
import platform.UIKit.UIViewController

fun embeddedMessagingListPage(): UIViewController {
    if (!SdkKoinIsolationContext.isInitialized()) {
        SdkKoinIsolationContext.init()
    }
    return ComposeUIViewController {
        ListPageView()
    }
}