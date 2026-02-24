package com.sap.ec.api.inapp

import platform.UIKit.UIViewController

interface IosInAppApi {
    val isPaused:Boolean
    suspend fun pause()
    suspend fun resume()
    fun InlineInAppView(viewId: String): UIViewController
}