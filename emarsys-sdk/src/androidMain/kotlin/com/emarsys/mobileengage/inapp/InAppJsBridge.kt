package com.emarsys.mobileengage.inapp

import android.webkit.JavascriptInterface
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel

class InAppJsBridge(private val actionFactoryApi: ActionFactoryApi<ActionModel>) {

    @JavascriptInterface
    fun close(jsonString: String) {

    }
}
