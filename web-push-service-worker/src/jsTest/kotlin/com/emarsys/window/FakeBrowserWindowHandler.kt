package com.emarsys.window

import web.serviceworker.WindowClient

class FakeBrowserWindowHandler : BrowserWindowHandlerApi {

    internal var shouldReturnWindowClient = false

    internal var openWindowWasCalled = false

    override suspend fun findOpenWindow(): WindowClient? {
        return if (shouldReturnWindowClient) getFakeWindowClient() else null
    }

    override suspend fun openWindow(url: String): WindowClient {
        openWindowWasCalled = true
        return getFakeWindowClient()
    }

    private fun getFakeWindowClient() =
        js(
            "{ focus: function(){ return Promise.resolve(null) } }"
        ).unsafeCast<WindowClient>()
}