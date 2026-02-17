package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.ui.window.ComposeUIViewController
import com.sap.ec.di.SdkKoinIsolationContext
import platform.UIKit.UIViewController

fun embeddedMessagingListPage(showFilters: Boolean = true): UIViewController {
    if (!SdkKoinIsolationContext.isInitialized()) {
        SdkKoinIsolationContext.init()
    }
    return ComposeUIViewController {
        ListPageView(showFilters)
    }
}