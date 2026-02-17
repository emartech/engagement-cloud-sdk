package com.sap.ec.window

import web.serviceworker.WindowClient

interface BrowserWindowHandlerApi {

    suspend fun findOpenWindow(): WindowClient?

    suspend fun openWindow(url: String): WindowClient?

}
