package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import com.emarsys.core.util.DownloaderApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import platform.UIKit.UIViewController

fun embeddedMessage(): UIViewController {
    return ComposeUIViewController {
        val imageDownloader = koin.get<DownloaderApi>()
        val coroutineScope = rememberCoroutineScope()

        ListPageView(ListPageViewModel(ListPageModel(), imageDownloader, coroutineScope = coroutineScope))
    }
}