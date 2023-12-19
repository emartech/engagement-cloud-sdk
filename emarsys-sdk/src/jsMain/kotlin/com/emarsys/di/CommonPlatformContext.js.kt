package com.emarsys.di

import kotlinx.browser.window
import org.w3c.dom.Storage

actual class CommonPlatformContext actual constructor() : PlatformContext {

    val storage: Storage = window.localStorage

}